package com.petcare.data.local.database

import androidx.room.TypeConverter
import com.petcare.data.local.enums.EtatRappel
import com.petcare.data.local.enums.StatutSoin
import com.petcare.data.local.enums.TypeEvenement

class EnumConverters {

    @TypeConverter
    fun fromTypeEvenement(value: TypeEvenement?): String? = value?.name

    @TypeConverter
    fun toTypeEvenement(value: String?): TypeEvenement? =
        value?.let { TypeEvenement.valueOf(it) }

    @TypeConverter
    fun fromStatutSoin(value: StatutSoin?): String? = value?.name

    @TypeConverter
    fun toStatutSoin(value: String?): StatutSoin? =
        value?.let { StatutSoin.valueOf(it) }

    @TypeConverter
    fun fromEtatRappel(value: EtatRappel?): String? = value?.name

    @TypeConverter
    fun toEtatRappel(value: String?): EtatRappel? =
        value?.let { EtatRappel.valueOf(it) }
}
