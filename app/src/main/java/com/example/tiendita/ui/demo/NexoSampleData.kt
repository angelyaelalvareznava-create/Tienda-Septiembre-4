package com.example.tiendita.ui.demo

import com.example.tiendita.ui.model.DirectoryItemUi

object NexoSampleData {
    val suppliers = listOf(
        DirectoryItemUi("sup_1", "Distribuidora Occidente", "Abarrotes y productos generales", "DO"),
        DirectoryItemUi("sup_2", "Empaques del Centro", "Cajas, bolsas y material de embalaje", "EC"),
        DirectoryItemUi("sup_3", "Limpieza Industrial GDL", "Productos y equipo de limpieza", "LI"),
        DirectoryItemUi("sup_4", "Papelería Nova", "Papelería y artículos administrativos", "PN"),
        DirectoryItemUi("sup_5", "Logística Rivera", "Transporte y entregas", "LR"),
        DirectoryItemUi("sup_6", "Tecnología Punto Norte", "Equipo y accesorios electrónicos", "TP"),
        DirectoryItemUi("sup_7", "Bebidas del Valle", "Bebidas y productos refrigerados", "BV"),
        DirectoryItemUi("sup_8", "Comercializadora del Pacífico", "Productos de mayoreo", "CP")
    )

    val employees = listOf(
        DirectoryItemUi("emp_1", "Ana Torres", "Encargada de almacén", "AT"),
        DirectoryItemUi("emp_2", "Luis Mendoza", "Auxiliar de inventario", "LM"),
        DirectoryItemUi("emp_3", "Karla Ramírez", "Administración", "KR"),
        DirectoryItemUi("emp_4", "Diego Navarro", "Recepción de mercancía", "DN"),
        DirectoryItemUi("emp_5", "Fernanda López", "Atención a clientes", "FL"),
        DirectoryItemUi("emp_6", "Javier Ortega", "Supervisor", "JO"),
        DirectoryItemUi("emp_7", "Sofía Castillo", "Compras", "SC"),
        DirectoryItemUi("emp_8", "Miguel Hernández", "Reparto y logística", "MH")
    )

    val clients = listOf(
        DirectoryItemUi("cli_1", "Abarrotes La Esquina", "Cliente frecuente", "AL"),
        DirectoryItemUi("cli_2", "Café del Parque", "Servicio de cafetería", "CP"),
        DirectoryItemUi("cli_3", "Mini Súper Zapopan", "Compra de mayoreo", "MZ"),
        DirectoryItemUi("cli_4", "Restaurante El Laurel", "Cliente empresarial", "RL"),
        DirectoryItemUi("cli_5", "Papelería Horizonte", "Cliente frecuente", "PH"),
        DirectoryItemUi("cli_6", "Tienda San Marcos", "Comercio minorista", "SM"),
        DirectoryItemUi("cli_7", "Cafetería Central", "Cliente empresarial", "CC"),
        DirectoryItemUi("cli_8", "Mercado Rivera", "Compra de mayoreo", "MR")
    )
}
