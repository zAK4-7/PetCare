package com.petcare.app.ui.nav

object Routes {
    const val Login = "login"
    const val App = "app"

    const val Home = "home"

    const val Agenda = "agenda"
    const val AddAppointment = "agenda/add"

    const val EditAppointment = "agenda/edit/{id}"
    fun editAppointment(id: Int) = "agenda/edit/$id"

    const val Pets = "pets"
    const val AddPet = "pets/add"

    const val EditPet = "pets/edit/{id}"
    fun editPet(id: Int) = "pets/edit/$id"

    const val Services = "services"
    const val Profile = "profile"
}
