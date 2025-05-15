-- Insertar usuarios
INSERT INTO USUARIO (IDUSUARIO, NOMBREUSUARIO, CLAVE) VALUES
('01', 'gerente', 'gerente123'),
('02', 'farmaceutico', 'farmaceutico123'),
('03', 'cajero', 'cajero123'),
('04', 'admin', '1234'),
('05', 'auxiliar', 'auxiliar123'),
('06', 'consultor', 'consultor123'),
('07', 'tester', 'tester123'),
('08', 'editor', 'editor123');

-- Insertar opciones en OPCIONCRUD
INSERT INTO OPCIONCRUD (IDOPCION, DESOPCION, NUMCRUD) VALUES
('001', 'Menu Direction', 0),
('002', 'Direction Create', 1),
('003', 'Direction Read', 2),
('004', 'Direction Update', 3),
('005', 'Direction Delete', 4),
('006', 'Menu Branch', 0),
('007', 'Branch Create', 1),
('008', 'Branch Read', 2),
('009', 'Branch Update', 3),
('010', 'Branch Delete', 4),
('011', 'Menu Existence Detail', 0),
('012', 'Existence Detail Add', 1),
('013', 'Existence Detail Read', 2),
('014', 'Existence Detail Update', 3),
('015', 'Existence Detail Delete', 4),
('016', 'Menu Sales', 0),
('017', 'Sales Create', 1),
('018', 'Sales Read', 2),
('019', 'Sales Update', 3),
('020', 'Sales Delete', 4),
('021', 'Menu Sales Details', 0),
('022', 'Sales Details Create', 1),
('023', 'Sales Details Read', 2),
('024', 'Sales Details Update', 3),
('025', 'Sales Details Delete', 4),
('026', 'Menu Sales Invoice', 0),
('027', 'Sales Invoice Create', 1),
('028', 'Sales Invoice Read', 2),
('029', 'Sales Invoice Update', 3),
('030', 'Sales Invoice Delete', 4),
('031', 'Menu Client', 0),
('032', 'Client Create', 1),
('033', 'Client Read', 2),
('034', 'Client Update', 3),
('035', 'Client Delete', 4),
('036', 'Menu Purchase', 0),
('037', 'Purchase Create', 1),
('038', 'Purchase Read', 2),
('039', 'Purchase Update', 3),
('040', 'Purchase Delete', 4),
('041', 'Menu Purchase Details', 0),
('042', 'Purchase Details Create', 1),
('043', 'Purchase Details Read', 2),
('044', 'Purchase Details Update', 3),
('045', 'Purchase Details Delete', 4),
('046', 'Menu Purchase Invoice', 0),
('047', 'Purchase Invoice Create', 1),
('048', 'Purchase Invoice Read', 2),
('049', 'Purchase Invoice Update', 3),
('050', 'Purchase Invoice Delete', 4),
('051', 'Menu Supplier', 0),
('052', 'Supplier Create', 1),
('053', 'Supplier Read', 2),
('054', 'Supplier Update', 3),
('055', 'Supplier Delete', 4),
('056', 'Menu Pharmaceutical Form', 0),
('057', 'Pharmaceutical Form Create', 1),
('058', 'Pharmaceutical Form Read', 2),
('059', 'Pharmaceutical Form Update', 3),
('060', 'Pharmaceutical Form Delete', 4),
('061', 'Menu Brand', 0),
('062', 'Brand Create', 1),
('063', 'Brand Read', 2),
('064', 'Brand Update', 3),
('065', 'Brand Delete', 4),
('066', 'Menu Route of Administration', 0),
('067', 'Route of Administration Create', 1),
('068', 'Route of Administration Read', 2),
('069', 'Route of Administration Update', 3),
('070', 'Route of Administration Delete', 4),
('071', 'Menu Prescription', 0),
('072', 'Prescription Create', 1),
('073', 'Prescription Read', 2),
('074', 'Prescription Update', 3),
('075', 'Prescription Delete', 4),
('076', 'Menu Doctor', 0),
('077', 'Doctor Create', 1),
('078', 'Doctor Read', 2),
('079', 'Doctor Update', 3),
('080', 'Doctor Delete', 4),
('081', 'Menu Item', 0),
('082', 'Item Create', 1),
('083', 'Item Read', 2),
('084', 'Item Update', 3),
('085', 'Item Delete', 4),
('086', 'Menu Category', 0),
('087', 'Category Create', 1),
('088', 'Category Read', 2),
('089', 'Category Update', 3),
('090', 'Category Delete', 4),
('091', 'Menu Subcategory', 0),
('092', 'Subcategory Create', 1),
('093', 'Subcategory Read', 2),
('094', 'Subcategory Update', 3),
('095', 'Subcategory Delete', 4);

-- Accesos para el usuario 'admin' (acceso total)
INSERT INTO ACCESOUSUARIO (IDUSUARIO, IDOPCION)
SELECT '04', IDOPCION FROM OPCIONCRUD;

-- Asignar accesos solo a menus y lectura (NUMCRUD = 0 o 2)
INSERT INTO ACCESOUSUARIO (IDUSUARIO, IDOPCION)
SELECT '06', IDOPCION
FROM OPCIONCRUD
WHERE NUMCRUD IN (0, 2);

-- Asignar accesos a tester1: solo Menús, Crear y Leer
INSERT INTO ACCESOUSUARIO (IDUSUARIO, IDOPCION)
SELECT '07', IDOPCION
FROM OPCIONCRUD
WHERE NUMCRUD IN (0, 1, 2);

-- Asignar accesos a editor1: Menú, Leer y Actualizar
INSERT INTO ACCESOUSUARIO (IDUSUARIO, IDOPCION)
SELECT '08', IDOPCION
FROM OPCIONCRUD
WHERE NUMCRUD IN (0, 2, 3);

-- Accesos para el usuario 'gerente' (manejo de compras, proveedores y lectura de inventario)
INSERT INTO ACCESOUSUARIO (IDUSUARIO, IDOPCION) VALUES
-- Existence Detail (lectura)
('01', '011'), ('01', '013'),
-- Purchase
('01', '036'), ('01', '037'), ('01', '038'), ('01', '039'), ('01', '040'),
-- Purchase Details
('01', '041'), ('01', '042'), ('01', '043'), ('01', '044'), ('01', '045'),
-- Purchase Invoice
('01', '046'), ('01', '047'), ('01', '048'), ('01', '049'), ('01', '050'),
-- Supplier
('01', '051'), ('01', '052'), ('01', '053'), ('01', '054'), ('01', '055'),
-- Lectura artículos/categorías
('01', '083'), ('01', '088'), ('01', '093');

-- Accesos para el usuario 'farmaceutico' (manejo de artículos, recetas, doctores, marcas)
INSERT INTO ACCESOUSUARIO (IDUSUARIO, IDOPCION) VALUES
-- Item
('02', '081'), ('02', '082'), ('02', '083'), ('02', '084'), ('02', '085'),
-- Category
('02', '086'), ('02', '087'), ('02', '088'), ('02', '089'), ('02', '090'),
-- Subcategory
('02', '091'), ('02', '092'), ('02', '093'), ('02', '094'), ('02', '095'),
-- Prescription
('02', '071'), ('02', '072'), ('02', '073'), ('02', '074'), ('02', '075'),
-- Brand
('02', '061'), ('02', '062'), ('02', '063'), ('02', '064'), ('02', '065'),
-- Pharmaceutical Form
('02', '056'), ('02', '057'), ('02', '058'), ('02', '059'), ('02', '060'),
-- Route of Administration
('02', '066'), ('02', '067'), ('02', '068'), ('02', '069'), ('02', '070'),
-- Doctor
('02', '076'), ('02', '077'), ('02', '078'), ('02', '079'), ('02', '080');

-- Accesos para el usuario 'cajero' (ventas y clientes)
INSERT INTO ACCESOUSUARIO (IDUSUARIO, IDOPCION) VALUES
-- Sales
('03', '016'), ('03', '017'), ('03', '018'), ('03', '019'), ('03', '020'),
-- Sales Details
('03', '021'), ('03', '022'), ('03', '023'), ('03', '024'), ('03', '025'),
-- Sales Invoice
('03', '026'), ('03', '027'), ('03', '028'), ('03', '029'), ('03', '030'),
-- Client
('03', '031'), ('03', '032'), ('03', '033'), ('03', '034'), ('03', '035');

-- Accesos para el usuario 'aux_inventario' (solo lectura inventario y artículos)
INSERT INTO ACCESOUSUARIO (IDUSUARIO, IDOPCION) VALUES
('05', '011'),
('05', '013'),
('05', '083'),
('05', '088'),
('05', '093');
