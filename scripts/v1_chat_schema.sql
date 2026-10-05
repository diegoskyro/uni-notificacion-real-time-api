-- ===============================================================================
-- Script DDL: Historial de Mensajes de Chat en Tiempo Real
-- Esquema: core
-- Sistema: unisimon_dev (SQL Server)
-- Convenciones: db-conventions (Singular, snake_case, auditoría completa)
-- ===============================================================================

USE [unisimon_dev];
GO

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'chat_historial' AND schema_id = SCHEMA_ID('core'))
BEGIN
    CREATE TABLE core.chat_historial (
        id INT IDENTITY(1,1) NOT NULL,
        uuid UNIQUEIDENTIFIER DEFAULT NEWID() NOT NULL,
        conversacion_uuid UNIQUEIDENTIFIER NOT NULL,
        emisor_uuid UNIQUEIDENTIFIER NOT NULL,
        receptor_uuid UNIQUEIDENTIFIER NULL,
        contenido VARCHAR(MAX) NOT NULL,
        tipo_emisor VARCHAR(20) DEFAULT 'SOLICITANTE' NOT NULL,
        es_activo BIT DEFAULT 1 NOT NULL,
        fecha_creacion DATETIME DEFAULT GETDATE() NOT NULL,
        usuario_creacion VARCHAR(100) NOT NULL,
        fecha_actualizacion DATETIME NULL,
        usuario_actualizacion VARCHAR(100) NULL,
        CONSTRAINT pk_chat_historial PRIMARY KEY CLUSTERED (id ASC),
        CONSTRAINT uk_chat_historial_uuid UNIQUE (uuid)
    );

    CREATE NONCLUSTERED INDEX ix_chat_historial_conversacion 
        ON core.chat_historial (conversacion_uuid ASC, fecha_creacion ASC);

    EXEC sp_addextendedproperty @name = N'MS_Description', @value = N'Historial plano de mensajes transmitidos en tiempo real entre solicitante y agente', @level0type = N'SCHEMA', @level0name = 'core', @level1type = N'TABLE', @level1name = 'chat_historial';
    EXEC sp_addextendedproperty @name = N'MS_Description', @value = N'Identificador clave primaria serial', @level0type = N'SCHEMA', @level0name = 'core', @level1type = N'TABLE', @level1name = 'chat_historial', @level2type = N'COLUMN', @level2name = 'id';
    EXEC sp_addextendedproperty @name = N'MS_Description', @value = N'UUID público del registro de mensaje', @level0type = N'SCHEMA', @level0name = 'core', @level1type = N'TABLE', @level1name = 'chat_historial', @level2type = N'COLUMN', @level2name = 'uuid';
    EXEC sp_addextendedproperty @name = N'MS_Description', @value = N'UUID de la sesión o conversación entre solicitante y agente', @level0type = N'SCHEMA', @level0name = 'core', @level1type = N'TABLE', @level1name = 'chat_historial', @level2type = N'COLUMN', @level2name = 'conversacion_uuid';
    EXEC sp_addextendedproperty @name = N'MS_Description', @value = N'UUID del usuario emisor del mensaje', @level0type = N'SCHEMA', @level0name = 'core', @level1type = N'TABLE', @level1name = 'chat_historial', @level2type = N'COLUMN', @level2name = 'emisor_uuid';
    EXEC sp_addextendedproperty @name = N'MS_Description', @value = N'UUID del usuario receptor del mensaje', @level0type = N'SCHEMA', @level0name = 'core', @level1type = N'TABLE', @level1name = 'chat_historial', @level2type = N'COLUMN', @level2name = 'receptor_uuid';
    EXEC sp_addextendedproperty @name = N'MS_Description', @value = N'Contenido textual del mensaje', @level0type = N'SCHEMA', @level0name = 'core', @level1type = N'TABLE', @level1name = 'chat_historial', @level2type = N'COLUMN', @level2name = 'contenido';
    EXEC sp_addextendedproperty @name = N'MS_Description', @value = N'Rol/Tipo de quien emite el mensaje (SOLICITANTE, AGENTE, SISTEMA)', @level0type = N'SCHEMA', @level0name = 'core', @level1type = N'TABLE', @level1name = 'chat_historial', @level2type = N'COLUMN', @level2name = 'tipo_emisor';
END
GO
