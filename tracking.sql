--------------------------------------------------------
-- TRACKING ENTREGA CletaEats
-- Script completo corregido
--------------------------------------------------------

--------------------------------------------------------
-- 1. Agregar columnas solo si no existen
--------------------------------------------------------

DECLARE
    V_COUNT NUMBER;
BEGIN
    SELECT COUNT(*)
    INTO V_COUNT
    FROM ALL_TAB_COLUMNS
    WHERE OWNER = 'CLETAEATS'
      AND TABLE_NAME = 'REPARTIDOR'
      AND COLUMN_NAME = 'LATITUD_ACTUAL';

    IF V_COUNT = 0 THEN
        EXECUTE IMMEDIATE '
            ALTER TABLE CLETAEATS.REPARTIDOR ADD (
                LATITUD_ACTUAL NUMBER(10,7)
            )
        ';
    END IF;

    SELECT COUNT(*)
    INTO V_COUNT
    FROM ALL_TAB_COLUMNS
    WHERE OWNER = 'CLETAEATS'
      AND TABLE_NAME = 'REPARTIDOR'
      AND COLUMN_NAME = 'LONGITUD_ACTUAL';

    IF V_COUNT = 0 THEN
        EXECUTE IMMEDIATE '
            ALTER TABLE CLETAEATS.REPARTIDOR ADD (
                LONGITUD_ACTUAL NUMBER(10,7)
            )
        ';
    END IF;

    SELECT COUNT(*)
    INTO V_COUNT
    FROM ALL_TAB_COLUMNS
    WHERE OWNER = 'CLETAEATS'
      AND TABLE_NAME = 'REPARTIDOR'
      AND COLUMN_NAME = 'PRECISION_METROS';

    IF V_COUNT = 0 THEN
        EXECUTE IMMEDIATE '
            ALTER TABLE CLETAEATS.REPARTIDOR ADD (
                PRECISION_METROS NUMBER(10,2)
            )
        ';
    END IF;

    SELECT COUNT(*)
    INTO V_COUNT
    FROM ALL_TAB_COLUMNS
    WHERE OWNER = 'CLETAEATS'
      AND TABLE_NAME = 'REPARTIDOR'
      AND COLUMN_NAME = 'ULTIMA_UBICACION_EN';

    IF V_COUNT = 0 THEN
        EXECUTE IMMEDIATE '
            ALTER TABLE CLETAEATS.REPARTIDOR ADD (
                ULTIMA_UBICACION_EN TIMESTAMP
            )
        ';
    END IF;
END;
/

--------------------------------------------------------
-- 2. Package spec
--------------------------------------------------------

--------------------------------------------------------
-- RECREAR PKG_TRACKING_ENTREGA DESDE CERO
--------------------------------------------------------

BEGIN
    EXECUTE IMMEDIATE 'DROP PACKAGE CLETAEATS.PKG_TRACKING_ENTREGA';
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE != -4043 THEN
            RAISE;
        END IF;
END;
/

--------------------------------------------------------
-- PACKAGE SPEC
--------------------------------------------------------

CREATE OR REPLACE PACKAGE CLETAEATS.PKG_TRACKING_ENTREGA AS

    PROCEDURE ACTUALIZAR_UBICACION_REPARTIDOR(
        P_AUTH_NAME           IN  VARCHAR2,
        P_PEDIDO_ID           IN  NUMBER,
        P_LATITUD             IN  NUMBER,
        P_LONGITUD            IN  NUMBER,
        P_PRECISION_METROS    IN  NUMBER,
        O_PEDIDO_ID           OUT NUMBER,
        O_REPARTIDOR_ID       OUT NUMBER,
        O_REPARTIDOR_NOMBRE   OUT VARCHAR2,
        O_ESTADO_PEDIDO       OUT VARCHAR2,
        O_LATITUD             OUT NUMBER,
        O_LONGITUD            OUT NUMBER,
        O_PRECISION_METROS    OUT NUMBER,
        O_ULTIMA_UBICACION_EN OUT TIMESTAMP
    );

    PROCEDURE OBTENER_TRACKING_CLIENTE(
        P_AUTH_NAME           IN  VARCHAR2,
        P_PEDIDO_ID           IN  NUMBER,
        O_PEDIDO_ID           OUT NUMBER,
        O_REPARTIDOR_ID       OUT NUMBER,
        O_REPARTIDOR_NOMBRE   OUT VARCHAR2,
        O_ESTADO_PEDIDO       OUT VARCHAR2,
        O_LATITUD             OUT NUMBER,
        O_LONGITUD            OUT NUMBER,
        O_PRECISION_METROS    OUT NUMBER,
        O_ULTIMA_UBICACION_EN OUT TIMESTAMP
    );

END PKG_TRACKING_ENTREGA;
/

--------------------------------------------------------
-- PACKAGE BODY
--------------------------------------------------------

CREATE OR REPLACE PACKAGE BODY CLETAEATS.PKG_TRACKING_ENTREGA AS

    FUNCTION OBTENER_USUARIO_ID(
        P_AUTH_NAME IN VARCHAR2
    ) RETURN NUMBER AS
        V_USUARIO_ID NUMBER;
    BEGIN
        IF P_AUTH_NAME IS NULL THEN
            RAISE_APPLICATION_ERROR(-20000, 'Usuario autenticado vacío.');
        END IF;

        BEGIN
            V_USUARIO_ID := TO_NUMBER(P_AUTH_NAME);
            RETURN V_USUARIO_ID;
        EXCEPTION
            WHEN VALUE_ERROR THEN
                NULL;
        END;

        BEGIN
            SELECT u.USUARIO_ID
            INTO V_USUARIO_ID
            FROM CLETAEATS.USUARIO u
            WHERE LOWER(u.CORREO) = LOWER(P_AUTH_NAME);

            RETURN V_USUARIO_ID;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                RAISE_APPLICATION_ERROR(
                    -20000,
                    'No se pudo identificar el usuario autenticado: ' || P_AUTH_NAME
                );
        END;
    END OBTENER_USUARIO_ID;


    PROCEDURE VALIDAR_COORDENADAS(
        P_LATITUD  IN NUMBER,
        P_LONGITUD IN NUMBER
    ) AS
    BEGIN
        IF P_LATITUD IS NULL OR P_LATITUD < -90 OR P_LATITUD > 90 THEN
            RAISE_APPLICATION_ERROR(-20001, 'Latitud inválida.');
        END IF;

        IF P_LONGITUD IS NULL OR P_LONGITUD < -180 OR P_LONGITUD > 180 THEN
            RAISE_APPLICATION_ERROR(-20002, 'Longitud inválida.');
        END IF;
    END VALIDAR_COORDENADAS;


    PROCEDURE CARGAR_TRACKING(
        P_PEDIDO_ID           IN  NUMBER,
        O_PEDIDO_ID           OUT NUMBER,
        O_REPARTIDOR_ID       OUT NUMBER,
        O_REPARTIDOR_NOMBRE   OUT VARCHAR2,
        O_ESTADO_PEDIDO       OUT VARCHAR2,
        O_LATITUD             OUT NUMBER,
        O_LONGITUD            OUT NUMBER,
        O_PRECISION_METROS    OUT NUMBER,
        O_ULTIMA_UBICACION_EN OUT TIMESTAMP
    ) AS
    BEGIN
        SELECT
            p.PEDIDO_ID,
            r.REPARTIDOR_ID,
            u.NOMBRE_COMPLETO,
            p.ESTADO,
            r.LATITUD_ACTUAL,
            r.LONGITUD_ACTUAL,
            r.PRECISION_METROS,
            r.ULTIMA_UBICACION_EN
        INTO
            O_PEDIDO_ID,
            O_REPARTIDOR_ID,
            O_REPARTIDOR_NOMBRE,
            O_ESTADO_PEDIDO,
            O_LATITUD,
            O_LONGITUD,
            O_PRECISION_METROS,
            O_ULTIMA_UBICACION_EN
        FROM CLETAEATS.PEDIDO p
        LEFT JOIN CLETAEATS.REPARTIDOR r
               ON r.REPARTIDOR_ID = p.REPARTIDOR_ID
        LEFT JOIN CLETAEATS.USUARIO u
               ON u.USUARIO_ID = r.USUARIO_ID
        WHERE p.PEDIDO_ID = P_PEDIDO_ID;

    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            RAISE_APPLICATION_ERROR(-20003, 'Pedido no encontrado.');
    END CARGAR_TRACKING;


    PROCEDURE ACTUALIZAR_UBICACION_REPARTIDOR(
        P_AUTH_NAME           IN  VARCHAR2,
        P_PEDIDO_ID           IN  NUMBER,
        P_LATITUD             IN  NUMBER,
        P_LONGITUD            IN  NUMBER,
        P_PRECISION_METROS    IN  NUMBER,
        O_PEDIDO_ID           OUT NUMBER,
        O_REPARTIDOR_ID       OUT NUMBER,
        O_REPARTIDOR_NOMBRE   OUT VARCHAR2,
        O_ESTADO_PEDIDO       OUT VARCHAR2,
        O_LATITUD             OUT NUMBER,
        O_LONGITUD            OUT NUMBER,
        O_PRECISION_METROS    OUT NUMBER,
        O_ULTIMA_UBICACION_EN OUT TIMESTAMP
    ) AS
        V_USUARIO_ID    NUMBER;
        V_REPARTIDOR_ID NUMBER;
        V_EXISTE        NUMBER;
    BEGIN
        V_USUARIO_ID := OBTENER_USUARIO_ID(P_AUTH_NAME);

        VALIDAR_COORDENADAS(P_LATITUD, P_LONGITUD);

        BEGIN
            SELECT r.REPARTIDOR_ID
            INTO V_REPARTIDOR_ID
            FROM CLETAEATS.REPARTIDOR r
            WHERE r.USUARIO_ID = V_USUARIO_ID;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                RAISE_APPLICATION_ERROR(-20004, 'El usuario autenticado no es repartidor.');
        END;

        SELECT COUNT(1)
        INTO V_EXISTE
        FROM CLETAEATS.PEDIDO p
        WHERE p.PEDIDO_ID = P_PEDIDO_ID
          AND p.REPARTIDOR_ID = V_REPARTIDOR_ID
          AND p.ESTADO IN ('EN_PREPARACION', 'EN_CAMINO');

        IF V_EXISTE = 0 THEN
            RAISE_APPLICATION_ERROR(
                -20005,
                'El pedido no está asignado a este repartidor o no permite tracking.'
            );
        END IF;

        UPDATE CLETAEATS.REPARTIDOR
        SET LATITUD_ACTUAL = P_LATITUD,
            LONGITUD_ACTUAL = P_LONGITUD,
            PRECISION_METROS = P_PRECISION_METROS,
            ULTIMA_UBICACION_EN = SYSTIMESTAMP
        WHERE REPARTIDOR_ID = V_REPARTIDOR_ID;

        CARGAR_TRACKING(
            P_PEDIDO_ID,
            O_PEDIDO_ID,
            O_REPARTIDOR_ID,
            O_REPARTIDOR_NOMBRE,
            O_ESTADO_PEDIDO,
            O_LATITUD,
            O_LONGITUD,
            O_PRECISION_METROS,
            O_ULTIMA_UBICACION_EN
        );
    END ACTUALIZAR_UBICACION_REPARTIDOR;


    PROCEDURE OBTENER_TRACKING_CLIENTE(
        P_AUTH_NAME           IN  VARCHAR2,
        P_PEDIDO_ID           IN  NUMBER,
        O_PEDIDO_ID           OUT NUMBER,
        O_REPARTIDOR_ID       OUT NUMBER,
        O_REPARTIDOR_NOMBRE   OUT VARCHAR2,
        O_ESTADO_PEDIDO       OUT VARCHAR2,
        O_LATITUD             OUT NUMBER,
        O_LONGITUD            OUT NUMBER,
        O_PRECISION_METROS    OUT NUMBER,
        O_ULTIMA_UBICACION_EN OUT TIMESTAMP
    ) AS
        V_USUARIO_ID NUMBER;
        V_EXISTE     NUMBER;
    BEGIN
        V_USUARIO_ID := OBTENER_USUARIO_ID(P_AUTH_NAME);

        SELECT COUNT(1)
        INTO V_EXISTE
        FROM CLETAEATS.PEDIDO p
        JOIN CLETAEATS.CLIENTE c
             ON c.CLIENTE_ID = p.CLIENTE_ID
        WHERE p.PEDIDO_ID = P_PEDIDO_ID
          AND c.USUARIO_ID = V_USUARIO_ID;

        IF V_EXISTE = 0 THEN
            RAISE_APPLICATION_ERROR(
                -20006,
                'El pedido no pertenece al cliente autenticado.'
            );
        END IF;

        CARGAR_TRACKING(
            P_PEDIDO_ID,
            O_PEDIDO_ID,
            O_REPARTIDOR_ID,
            O_REPARTIDOR_NOMBRE,
            O_ESTADO_PEDIDO,
            O_LATITUD,
            O_LONGITUD,
            O_PRECISION_METROS,
            O_ULTIMA_UBICACION_EN
        );
    END OBTENER_TRACKING_CLIENTE;

END PKG_TRACKING_ENTREGA;
/

SHOW ERRORS PACKAGE CLETAEATS.PKG_TRACKING_ENTREGA;
SHOW ERRORS PACKAGE BODY CLETAEATS.PKG_TRACKING_ENTREGA;

