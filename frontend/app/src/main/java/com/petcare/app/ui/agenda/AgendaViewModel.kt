package com.petcare.app.ui.agenda

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.petcare.app.data.model.AppointmentCreateRequest
import com.petcare.app.data.model.AppointmentDto
import com.petcare.app.data.model.AppointmentUpdateRequest
import com.petcare.app.data.repo.AgendaRepository
import com.petcare.app.worker.ReminderScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.*
import java.time.format.DateTimeFormatter

data class AgendaUiState(
    val loading: Boolean = false,
    val items: List<AppointmentDto> = emptyList(),
    val error: String? = null
)

class AgendaViewModel(
    private val repo: AgendaRepository = AgendaRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AgendaUiState())
    val uiState: StateFlow<AgendaUiState> = _uiState.asStateFlow()

    // ✅ on stocke le context (ApplicationContext) une seule fois
    private var appContext: Context? = null
    fun init(context: Context) {
        appContext = context.applicationContext
    }

    fun loadAgenda() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = null)

            runCatching { repo.fetchAppointments() }
                .onSuccess { list ->
                    _uiState.value = AgendaUiState(loading = false, items = list)
                }
                .onFailure { e ->
                    _uiState.value = AgendaUiState(
                        loading = false,
                        items = emptyList(),
                        error = e.message ?: "Erreur chargement agenda"
                    )
                }
        }
    }

    fun createAppointment(
        body: AppointmentCreateRequest,
        onDone: (AppointmentDto) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = null)

            runCatching { repo.createAppointment(body) }
                .onSuccess { created ->
                    val refreshed = runCatching { repo.fetchAppointments() }.getOrNull().orEmpty()
                    _uiState.value = AgendaUiState(loading = false, items = refreshed)

                    // ✅ schedule reminders (7j / 24h / 30min)
                    scheduleRemindersFor(created)

                    onDone(created)
                }
                .onFailure { e ->
                    val msg = e.message ?: "Erreur création"
                    _uiState.value = _uiState.value.copy(loading = false, error = msg)
                    onError(msg)
                }
        }
    }

    fun updateAppointment(
        id: Int,
        body: AppointmentUpdateRequest,
        onDone: (AppointmentDto) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = null)

            runCatching { repo.updateAppointment(id, body) }
                .onSuccess { updated ->
                    val refreshed = runCatching { repo.fetchAppointments() }.getOrNull().orEmpty()
                    _uiState.value = AgendaUiState(loading = false, items = refreshed)

                    // ✅ replace reminders
                    appContext?.let { ctx ->
                        ReminderScheduler.cancelAll(ctx, updated.id)
                    }
                    scheduleRemindersFor(updated)

                    onDone(updated)
                }
                .onFailure { e ->
                    val msg = e.message ?: "Erreur modification"
                    _uiState.value = _uiState.value.copy(loading = false, error = msg)
                    onError(msg)
                }
        }
    }

    fun deleteAppointment(
        id: Int,
        onDone: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = null)

            runCatching { repo.deleteAppointment(id) }
                .onSuccess {
                    // ✅ cancel reminders
                    appContext?.let { ctx ->
                        ReminderScheduler.cancelAll(ctx, id)
                    }

                    val refreshed = runCatching { repo.fetchAppointments() }.getOrNull().orEmpty()
                    _uiState.value = AgendaUiState(loading = false, items = refreshed)

                    onDone()
                }
                .onFailure { e ->
                    val msg = e.message ?: "Erreur suppression"
                    _uiState.value = _uiState.value.copy(loading = false, error = msg)
                    onError(msg)
                }
        }
    }

    // ----------------- helpers -----------------

    private fun scheduleRemindersFor(appt: AppointmentDto) {
        val ctx = appContext ?: return

        val (date, time) = isoToLocalDateTime(appt.startAt) ?: return

        ReminderScheduler.scheduleAll(
            context = ctx,
            appointmentId = appt.id,
            title = appt.title,
            date = date, // yyyy-MM-dd
            time = time  // HH:mm
        )
    }

    private fun isoToLocalDateTime(iso: String): Pair<String, String>? {
        val zone = ZoneId.systemDefault()

        // ✅ supporte "2025-..+01:00" et "2025-..Z"
        val zdt: ZonedDateTime = try {
            OffsetDateTime.parse(iso).atZoneSameInstant(zone)
        } catch (_: Throwable) {
            try {
                Instant.parse(iso).atZone(zone)
            } catch (_: Throwable) {
                return null
            }
        }

        val date = zdt.toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val time = zdt.toLocalTime().withSecond(0).withNano(0)
            .format(DateTimeFormatter.ofPattern("HH:mm"))

        return date to time
    }
}
