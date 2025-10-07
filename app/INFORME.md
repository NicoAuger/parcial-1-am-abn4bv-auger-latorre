# INFORME - PARCIAL 1: APLICACIONES MÓVILES

**Instituto:** Escuela Da Vinci  
**Materia:** Aplicaciones Móviles  
**Profesor:** Sergio Daniel Medina  
**Comisión:** ACN4BV  
**Integrantes:** Nicolás Auger – Javier Latorre  
**Repositorio:** *Mis Gastos Diarios*  
**Nombre del proyecto:** *Mis Gastos Diarios*  

---

## 1. Descripción del proyecto

**Mis Gastos Diarios** es una aplicación móvil desarrollada en **Android Studio con Java**, cuyo objetivo es permitir al usuario **registrar y controlar sus gastos diarios** en relación con un presupuesto establecido.  

La aplicación permite fijar un presupuesto inicial, agregar gastos categorizados (como *Comida, Transporte, Hogar, Ocio, Salud, Educación, Otros*), y visualizar en tiempo real cuánto ha gastado y cuánto le resta disponible.

El diseño busca mantener una **interfaz simple y clara**, ideal para una primera versión funcional y de fácil comprensión, cumpliendo con las pautas de usabilidad y legibilidad esperadas para un trabajo académico inicial.

---

## 2. Pantalla desarrollada

El proyecto selecciona **una única pantalla principal**, cumpliendo con la consigna del parcial.  
Esta pantalla incluye:

- **Campo de presupuesto inicial.**  
- **Resumen automático:** Presupuesto total, Gastado y Restante.  
- **Sección para agregar gastos:** Monto, Categoría (Spinner) y Nota opcional.  
- **Botón “Agregar”** para incorporar gastos.  
- **Listado dinámico de gastos**, dentro de un `ScrollView`, generado desde código Java.  
- **Botón “Eliminar”** para borrar gastos del listado.  

Toda la interfaz fue maquetada utilizando `ConstraintLayout` como contenedor raíz y un `LinearLayout` horizontal para el ingreso de datos.  
El resultado es una aplicación funcional, fluida y con respuestas visuales inmediatas ante la interacción del usuario.

---

## 3. Flujo de uso

1. El usuario ingresa un **presupuesto inicial** y presiona “Fijar presupuesto”.  
2. Completa los campos **Monto**, **Categoría** y **Nota (opcional)**.  
3. Presiona **“Agregar”**, y el gasto se muestra en la lista inferior.  
4. El sistema actualiza automáticamente los valores de **Gastado** y **Restante**.  
5. Si desea eliminar un gasto, presiona **“Eliminar”** junto al ítem correspondiente.  
6. Puede seguir agregando o quitando gastos según sea necesario.  

Este flujo representa la dinámica básica de control de gastos diarios de forma rápida y visual, priorizando la claridad en la interacción y la actualización en tiempo real de los datos más relevantes.

---

## 4. Lógica y estructura del proyecto

### 4.1 Organización general

- **Lenguaje:** Java  
- **Activity principal:** `MainActivity.java`  
- **Layout raíz:** `activity_main.xml`  
- **Estructura:**

app/
├── java/com/nauger/misgastosdiarios/MainActivity.java
├── res/
│ ├── layout/activity_main.xml
│ ├── values/strings.xml
│ ├── values/colors.xml
│ ├── values/dimens.xml
│ └── values/arrays.xml


### 4.2 Lógica de funcionamiento

1. **Presupuesto inicial:**  
 El usuario ingresa un monto y lo fija con un botón.  
 - Se guarda el valor en una variable interna `budget`.  
 - Se bloquea el campo de texto para evitar cambios accidentales.  
 - Se habilita la carga de nuevos gastos.  

2. **Carga de gastos:**  
 - Cada gasto contiene: `monto`, `categoría` (Spinner) y `nota` opcional.  
 - Al presionar **Agregar**, se crea dinámicamente un nuevo `LinearLayout` con los datos del gasto y un botón **Eliminar**.  
 - Este nuevo elemento se inserta en un contenedor dentro de un `ScrollView`.  

3. **Actualización automática del resumen:**  
 - Cada vez que se agrega o elimina un gasto, se recalculan los valores de **Gastado** y **Restante**.  
 - Se actualizan los `TextView` correspondientes con formato de moneda y los colores de advertencia si el restante llega a cero.  

4. **Eliminación de gastos:**  
 - Cada gasto creado dinámicamente incluye un botón que remueve su propio `View` del contenedor principal.  
 - Al hacerlo, también se actualizan los totales en el resumen.  

5. **Control de categorías:**  
 - El `Spinner` carga una lista fija de categorías definidas en `arrays.xml`.  
 - Cada gasto agregado se asocia a una categoría seleccionada para permitir estadísticas futuras.

6. **Gestión de estado:**  
 - Los datos se mantienen en memoria mientras la app está activa.  
 - No se implementa persistencia aún (ni `SharedPreferences` ni base de datos), ya que no es requerida para esta etapa.

---

## 5. Conclusión

El desarrollo de *Mis Gastos Diarios* permitió aplicar los conceptos fundamentales del diseño de interfaces móviles en Android, integrando estructuras como `ConstraintLayout` y `LinearLayout`, la creación dinámica de elementos desde Java y el manejo de eventos en botones.  

Durante el proceso se buscó mantener una arquitectura limpia y funcional, centrada en la experiencia del usuario. El proyecto cumple con los requisitos mínimos establecidos en la consigna y sienta una base sólida para futuras ampliaciones, como la incorporación de gráficos, persistencia de datos o personalización visual.  

Este trabajo sirvió como ejercicio de consolidación de conocimientos en el desarrollo mobile con Java, reforzando la lógica de eventos, el diseño adaptable y la organización de recursos dentro del entorno Android Studio.
