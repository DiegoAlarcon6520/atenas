package cl.duoc.atenas.di

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import cl.duoc.atenas.AtenasApp

/** Acceso corto al contenedor de dependencias desde las fábricas de ViewModel. */
val CreationExtras.contenedor: AppContainer
    get() = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as AtenasApp).contenedor
