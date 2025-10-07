package com.nauger.misgastosdiarios

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

/* Prueba instrumental que se ejecuta en un dispositivo o emulador Android.
   Verifica que el contexto de la aplicación corresponda al paquete esperado. */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    /* Comprueba que el nombre del paquete del contexto de la app sea correcto. */
    @Test
    fun useAppContext() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.nauger.misgastosdiarios", appContext.packageName)
    }
}
