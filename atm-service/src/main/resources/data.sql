-- Datos de prueba del ATM (base H2 en memoria).
-- Se cargan solos al iniciar: application.properties ya tiene
-- spring.jpa.defer-datasource-initialization=true y H2 es embebida.

-- Tarjetas (ids 1 a 5)
INSERT INTO tarjetas (numero, activa) VALUES ('4000111122223333', TRUE);   -- 1 Ana, activa, 2 cuentas
INSERT INTO tarjetas (numero, activa) VALUES ('4000555566668888', TRUE);   -- 2 Bruno, activa, 2 cuentas
INSERT INTO tarjetas (numero, activa) VALUES ('4000999988887777', FALSE);  -- 3 Carla, INACTIVA
INSERT INTO tarjetas (numero, activa) VALUES ('4000222233334444', TRUE);   -- 4 Diego, activa, 2 cuentas
INSERT INTO tarjetas (numero, activa) VALUES ('4000666677778888', TRUE);   -- 5 Elena, activa, saldo alto

-- Cuentas (ids 1 a 8)
INSERT INTO cuentas (numero, cbu, saldo) VALUES ('0000000001', '0170000000000000000001',   1000.00); -- 1 Ana
INSERT INTO cuentas (numero, cbu, saldo) VALUES ('0000000002', '0170000000000000000002',     50.00); -- 2 Bruno
INSERT INTO cuentas (numero, cbu, saldo) VALUES ('0000000003', '0170000000000000000003',      0.00); -- 3 Carla, saldo cero
INSERT INTO cuentas (numero, cbu, saldo) VALUES ('0000000004', '0170000000000000000004',  25000.75); -- 4 Ana, 2ª cuenta
INSERT INTO cuentas (numero, cbu, saldo) VALUES ('0000000005', '0170000000000000000005',   3500.00); -- 5 Diego
INSERT INTO cuentas (numero, cbu, saldo) VALUES ('0000000006', '0170000000000000000006',    120.40); -- 6 Diego, 2ª cuenta
INSERT INTO cuentas (numero, cbu, saldo) VALUES ('0000000007', '0170000000000000000007', 999999.99); -- 7 Elena, saldo alto
INSERT INTO cuentas (numero, cbu, saldo) VALUES ('0000000008', '0170000000000000000008',     10.00); -- 8 Bruno, 2ª cuenta

-- Relación tarjeta-cuenta
INSERT INTO tarjeta_cuenta (tarjeta_id, cuenta_id) VALUES (1, 1);
INSERT INTO tarjeta_cuenta (tarjeta_id, cuenta_id) VALUES (1, 4);
INSERT INTO tarjeta_cuenta (tarjeta_id, cuenta_id) VALUES (2, 2);
INSERT INTO tarjeta_cuenta (tarjeta_id, cuenta_id) VALUES (2, 8);
INSERT INTO tarjeta_cuenta (tarjeta_id, cuenta_id) VALUES (3, 3);
INSERT INTO tarjeta_cuenta (tarjeta_id, cuenta_id) VALUES (4, 5);
INSERT INTO tarjeta_cuenta (tarjeta_id, cuenta_id) VALUES (4, 6);
INSERT INTO tarjeta_cuenta (tarjeta_id, cuenta_id) VALUES (5, 7);