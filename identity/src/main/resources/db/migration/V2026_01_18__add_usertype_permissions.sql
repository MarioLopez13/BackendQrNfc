-- =====================================================
-- Migration: Define permissions for DOCTORS by medical area
-- Date: 2026-01-18
-- Description: Complete permissions for medical doctors
-- =====================================================

-- Clear existing DOCTORS permissions
DELETE FROM identity.user_type_permission WHERE user_type = 'DOCTORS';

-- =====================================================
-- =====================================================
--     ÁREA 1: CONSULTA EXTERNA / ATENCIÓN AMBULATORIA
-- =====================================================
-- =====================================================

-- Acceso al módulo de consulta médica
INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'DOCTORS', id FROM identity.permission
WHERE code = 'CALENDARIO:GESTION-CONSULTA-MEDICA';

-- Actualización de consultas (agenda de hoy)
INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'DOCTORS', id FROM identity.permission
WHERE code = 'ATENCION MEDICA:ACTUALIZACION-CONSULTA:MENU';

-- Datos generales de consulta
INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'DOCTORS', id FROM identity.permission
WHERE code = 'ATENCION MEDICA:CONSULTA-MEDICA:DATOS-GENERALES-CREAR';

INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'DOCTORS', id FROM identity.permission
WHERE code = 'ATENCION MEDICA:CONSULTA-MEDICA:EDITAR';

-- Ver todas las consultas (agenda completa)
INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'DOCTORS', id FROM identity.permission
WHERE code = 'ATENCION MEDICA:CONSULTA-MEDICA:VER-TODAS';

-- Signos Vitales en consulta
INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'DOCTORS', id FROM identity.permission
WHERE code = 'ATENCION MEDICA:CONSULTA-MEDICA:SIGNOS-VITALES:CREAR';

INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'DOCTORS', id FROM identity.permission
WHERE code = 'ATENCION MEDICA:CONSULTA-MEDICA:SIGNOS-VITALES:EDITAR';

-- Antecedentes Personales/Familiares (APP/APF)
INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'DOCTORS', id FROM identity.permission
WHERE code = 'ATENCION MEDICA:CONSULTA-MEDICA:APP-APF:CREAR';

INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'DOCTORS', id FROM identity.permission
WHERE code = 'ATENCION MEDICA:CONSULTA-MEDICA:APP-APF:EDITAR';

-- Exámenes en consulta
INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'DOCTORS', id FROM identity.permission
WHERE code = 'ATENCION MEDICA:CONSULTA-MEDICA:EXAMENES:CREAR';

-- Tratamientos/Recetas médicas
INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'DOCTORS', id FROM identity.permission
WHERE code = 'ATENCION MEDICA:CONSULTA-MEDICA:TRATAMIENTOS:CREAR';

INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'DOCTORS', id FROM identity.permission
WHERE code = 'ATENCION MEDICA:CONSULTA-MEDICA:TRATAMIENTOS:EDITAR';

-- Optometría (especialidad oftalmología)
INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'DOCTORS', id FROM identity.permission
WHERE code = 'ATENCION MEDICA:CONSULTA-MEDICA:OPTOMETRIA:CREAR';

INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'DOCTORS', id FROM identity.permission
WHERE code = 'ATENCION MEDICA:CONSULTA-MEDICA:OPTOMETRIA:EDITAR';


-- =====================================================
-- =====================================================
--     ÁREA 2: EMERGENCIA
-- =====================================================
-- =====================================================

-- Acceso general a emergencia
INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'DOCTORS', id FROM identity.permission
WHERE code = 'EMERGENCIA:EMERGENCIA:GESTIONAR';

-- Admisión de emergencia
INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'DOCTORS', id FROM identity.permission
WHERE code = 'EMERGENCIA:ADMISION:CREAR';

INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'DOCTORS', id FROM identity.permission
WHERE code = 'EMERGENCIA:ADMISION:EDITAR';

INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'DOCTORS', id FROM identity.permission
WHERE code = 'EMERGENCIA:ADMISION:GESTIONAR';

-- Anamnesis (historia clínica de emergencia)
INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'DOCTORS', id FROM identity.permission
WHERE code = 'EMERGENCIA:ANM:GESTIONAR';

-- Diagnóstico de emergencia
INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'DOCTORS', id FROM identity.permission
WHERE code = 'EMERGENCIA:DIAGNOSTICO:GESTIONAR';

-- Tratamiento de emergencia
INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'DOCTORS', id FROM identity.permission
WHERE code = 'EMERGENCIA:TRATAMIENTO:GESTIONAR';

-- Evoluciones del paciente
INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'DOCTORS', id FROM identity.permission
WHERE code = 'EMERGENCIA:EVOLUCIONES';

-- Interconsultas (solicitar opinión de otro especialista)
INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'DOCTORS', id FROM identity.permission
WHERE code = 'EMERGENCIA:INTERCONSULTA:GESTIONAR';

-- Epicrisis (resumen de alta)
INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'DOCTORS', id FROM identity.permission
WHERE code = 'EMERGENCIA:EPICRISIS:GESTIONAR';

-- Cirugía desde emergencia
INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'DOCTORS', id FROM identity.permission
WHERE code = 'EMERGENCIA:CIRUGIA:GESTIONAR';


-- =====================================================
-- =====================================================
--     ÁREA 3: CIRUGÍA
-- =====================================================
-- =====================================================

-- Acceso al módulo de cirugía
INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'DOCTORS', id FROM identity.permission
WHERE code = 'ATENCION MEDICA:CIRUGIA:MENU';

-- Crear planificación quirúrgica
INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'DOCTORS', id FROM identity.permission
WHERE code = 'ATENCION MEDICA:CIRUGIA:CREAR';

-- Editar planificación quirúrgica
INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'DOCTORS', id FROM identity.permission
WHERE code = 'ATENCION MEDICA:CIRUGIA:EDITAR';


-- =====================================================
-- =====================================================
--     ÁREA 4: HOSPITALIZACIÓN
-- =====================================================
-- =====================================================

-- Acceso al módulo de hospitalización
INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'DOCTORS', id FROM identity.permission
WHERE code = 'ATENCION MEDICA:HOSPITALIZACION:MENU';

-- Recibir paciente hospitalizado
INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'DOCTORS', id FROM identity.permission
WHERE code = 'ATENCION MEDICA:HOSPITALIZACION:RECIBIR-PACIENTE';

-- Transferir paciente entre salas
INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'DOCTORS', id FROM identity.permission
WHERE code = 'ATENCION MEDICA:HOSPITALIZACION:TRANSFERIR-PACIENTE';

-- Ver evoluciones del paciente
INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'DOCTORS', id FROM identity.permission
WHERE code = 'ATENCION MEDICA:HOSPITALIZACION:MOSTRAR-EVOLUCION';

-- Ver tratamientos del paciente
INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'DOCTORS', id FROM identity.permission
WHERE code = 'ATENCION MEDICA:HOSPITALIZACION:MOSTRAR-TRATAMIENTO';


-- =====================================================
-- =====================================================
--     ÁREA 5: ADMISIÓN
-- =====================================================
-- =====================================================

-- Acceso al módulo de admisión
INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'DOCTORS', id FROM identity.permission
WHERE code = 'ATENCION MEDICA:ADMISION:MENU';


-- =====================================================
-- =====================================================
--     ÁREA 6: CALENDARIO Y CITAS
-- =====================================================
-- =====================================================

-- Gestión de calendario (configurar disponibilidad)
INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'DOCTORS', id FROM identity.permission
WHERE code = 'CALENDARIO:GESTION-CALENDARIO';

-- Gestión de citas
INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'DOCTORS', id FROM identity.permission
WHERE code = 'CALENDARIO:GESTION-CITAS';


-- =====================================================
-- =====================================================
--     ÁREA 7: EXÁMENES DE LABORATORIO
-- =====================================================
-- =====================================================

-- Acceso al módulo de exámenes
INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'DOCTORS', id FROM identity.permission
WHERE code = 'EXAMENES:EXAMENES-LABORATORIO:MENU';

-- Subir resultados de exámenes
INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'DOCTORS', id FROM identity.permission
WHERE code = 'EXAMENES:EXAMENES-LABORATORIO:SUBIR-ARCHIVO';


-- =====================================================
-- =====================================================
--     ÁREA 8: GESTIÓN DE PACIENTES
-- =====================================================
-- =====================================================

-- Gestión general de pacientes
INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'DOCTORS', id FROM identity.permission
WHERE code = 'PACIENTE:GESTION-PACIENTES';

-- Consulta médica desde ficha del paciente
INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'DOCTORS', id FROM identity.permission
WHERE code = 'PERSONAS:PACIENTES:CONSULTA-MEDICA:CREAR';

INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'DOCTORS', id FROM identity.permission
WHERE code = 'PERSONAS:PACIENTES:CONSULTA-MEDICA:EDITAR';

-- Signos vitales desde ficha del paciente
INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'DOCTORS', id FROM identity.permission
WHERE code = 'PERSONAS:PACIENTES:SIGNOS-VITALES:CREAR';

INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'DOCTORS', id FROM identity.permission
WHERE code = 'PERSONAS:PACIENTES:SIGNOS-VITALES:EDITAR';

-- Vacunas desde ficha del paciente
INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'DOCTORS', id FROM identity.permission
WHERE code = 'PERSONAS:PACIENTES:VACUNAS:CREAR';

INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'DOCTORS', id FROM identity.permission
WHERE code = 'PERSONAS:PACIENTES:VACUNAS:EDITAR';

-- Aseguradoras del paciente
INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'DOCTORS', id FROM identity.permission
WHERE code = 'PERSONAS:PACIENTES:ASEGURADORAS:CREAR';

INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'DOCTORS', id FROM identity.permission
WHERE code = 'PERSONAS:PACIENTES:ASEGURADORAS:EDITAR';

-- Antecedentes del paciente
INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'DOCTORS', id FROM identity.permission
WHERE code = 'PERSONAS:PACIENTES:APP-APF:EDITAR';

-- Historia clínica
INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'DOCTORS', id FROM identity.permission
WHERE code = 'PERSONAS:PACIENTES:HISTORIA-CLINICA:EDITAR';


-- =====================================================
-- =====================================================
--     ÁREA 9: NOMENCLADORES (Solo lectura)
-- =====================================================
-- =====================================================

-- Medicamentos (para recetar)
INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'DOCTORS', id FROM identity.permission
WHERE code = 'NOMENCLADORES:MEDICAMENTOS:MENU';

-- Procedimientos
INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'DOCTORS', id FROM identity.permission
WHERE code = 'NOMENCLADORES:PROCEDIMIENTOS:MENU';

-- Servicios
INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'DOCTORS', id FROM identity.permission
WHERE code = 'NOMENCLADORES:SERVICIO:MENU';

-- Tipos de servicio
INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'DOCTORS', id FROM identity.permission
WHERE code = 'NOMENCLADORES:TIPO-DE-SERVICIO:MENU';

-- Exámenes disponibles
INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'DOCTORS', id FROM identity.permission
WHERE code = 'NOMENCLADORES:EXAMENES:MENU';

-- Tipos de exámenes de laboratorio
INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'DOCTORS', id FROM identity.permission
WHERE code = 'NOMENCLADORES:TIPO-DE-EXAMENES-DE-LABORATORIO:MENU';

-- Vacunas (catálogo)
INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'DOCTORS', id FROM identity.permission
WHERE code = 'NOMENCLADORES:VACUNAS:MENU';

-- Aseguradoras (referencia)
INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'DOCTORS', id FROM identity.permission
WHERE code = 'NOMENCLADORES:ASEGURADORA:MENU';


-- =====================================================
-- =====================================================
--     ÁREA 10: INICIO / DASHBOARD
-- =====================================================
-- =====================================================

-- Página de inicio
INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'DOCTORS', id FROM identity.permission
WHERE code = 'INICIO:INICIO';

-- Plan de trabajo del día
INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'DOCTORS', id FROM identity.permission
WHERE code = 'INICIO:PLAN-TRABAJO';


-- #############################################################################
-- #############################################################################
-- #############################################################################
--                           PERMISOS PARA NURSES
-- #############################################################################
-- #############################################################################
-- #############################################################################

-- Clear existing NURSES permissions
DELETE FROM identity.user_type_permission WHERE user_type = 'NURSES';

-- =====================================================
--     NURSES - ÁREA 1: CONSULTA EXTERNA (Apoyo)
-- =====================================================

-- Acceso al módulo de consulta médica (solo visualización)
INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'NURSES', id FROM identity.permission
WHERE code = 'CALENDARIO:GESTION-CONSULTA-MEDICA';

-- Ver agenda de consultas
INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'NURSES', id FROM identity.permission
WHERE code = 'ATENCION MEDICA:ACTUALIZACION-CONSULTA:MENU';

-- Signos Vitales - CREAR Y EDITAR (responsabilidad principal de enfermería)
INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'NURSES', id FROM identity.permission
WHERE code = 'ATENCION MEDICA:CONSULTA-MEDICA:SIGNOS-VITALES:CREAR';

INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'NURSES', id FROM identity.permission
WHERE code = 'ATENCION MEDICA:CONSULTA-MEDICA:SIGNOS-VITALES:EDITAR';

-- Antecedentes (pueden registrar información básica)
INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'NURSES', id FROM identity.permission
WHERE code = 'ATENCION MEDICA:CONSULTA-MEDICA:APP-APF:CREAR';

INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'NURSES', id FROM identity.permission
WHERE code = 'ATENCION MEDICA:CONSULTA-MEDICA:APP-APF:EDITAR';

-- =====================================================
--     NURSES - ÁREA 2: EMERGENCIA (Apoyo crítico)
-- =====================================================

-- Acceso general a emergencia
INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'NURSES', id FROM identity.permission
WHERE code = 'EMERGENCIA:EMERGENCIA:GESTIONAR';

-- Admisión de emergencia
INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'NURSES', id FROM identity.permission
WHERE code = 'EMERGENCIA:ADMISION:CREAR';

INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'NURSES', id FROM identity.permission
WHERE code = 'EMERGENCIA:ADMISION:EDITAR';

INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'NURSES', id FROM identity.permission
WHERE code = 'EMERGENCIA:ADMISION:GESTIONAR';

-- Anamnesis (apoyo en registro)
INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'NURSES', id FROM identity.permission
WHERE code = 'EMERGENCIA:ANM:GESTIONAR';

-- Evoluciones del paciente (registrar notas de enfermería)
INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'NURSES', id FROM identity.permission
WHERE code = 'EMERGENCIA:EVOLUCIONES';

-- =====================================================
--     NURSES - ÁREA 4: HOSPITALIZACIÓN (Principal)
-- =====================================================

-- Acceso al módulo de hospitalización
INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'NURSES', id FROM identity.permission
WHERE code = 'ATENCION MEDICA:HOSPITALIZACION:MENU';

-- Recibir paciente hospitalizado
INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'NURSES', id FROM identity.permission
WHERE code = 'ATENCION MEDICA:HOSPITALIZACION:RECIBIR-PACIENTE';

-- Ver evoluciones del paciente
INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'NURSES', id FROM identity.permission
WHERE code = 'ATENCION MEDICA:HOSPITALIZACION:MOSTRAR-EVOLUCION';

-- Ver tratamientos (para administrar medicamentos)
INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'NURSES', id FROM identity.permission
WHERE code = 'ATENCION MEDICA:HOSPITALIZACION:MOSTRAR-TRATAMIENTO';

-- =====================================================
--     NURSES - ÁREA 5: ADMISIÓN
-- =====================================================

-- Acceso al módulo de admisión
INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'NURSES', id FROM identity.permission
WHERE code = 'ATENCION MEDICA:ADMISION:MENU';

-- =====================================================
--     NURSES - ÁREA 7: EXÁMENES DE LABORATORIO
-- =====================================================

-- Acceso al módulo de exámenes
INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'NURSES', id FROM identity.permission
WHERE code = 'EXAMENES:EXAMENES-LABORATORIO:MENU';

-- Subir resultados de exámenes
INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'NURSES', id FROM identity.permission
WHERE code = 'EXAMENES:EXAMENES-LABORATORIO:SUBIR-ARCHIVO';

-- =====================================================
--     NURSES - ÁREA 8: GESTIÓN DE PACIENTES
-- =====================================================

-- Gestión general de pacientes
INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'NURSES', id FROM identity.permission
WHERE code = 'PACIENTE:GESTION-PACIENTES';

-- Signos vitales desde ficha del paciente
INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'NURSES', id FROM identity.permission
WHERE code = 'PERSONAS:PACIENTES:SIGNOS-VITALES:CREAR';

INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'NURSES', id FROM identity.permission
WHERE code = 'PERSONAS:PACIENTES:SIGNOS-VITALES:EDITAR';

-- Vacunas (administración de vacunas)
INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'NURSES', id FROM identity.permission
WHERE code = 'PERSONAS:PACIENTES:VACUNAS:CREAR';

INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'NURSES', id FROM identity.permission
WHERE code = 'PERSONAS:PACIENTES:VACUNAS:EDITAR';

-- Antecedentes del paciente
INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'NURSES', id FROM identity.permission
WHERE code = 'PERSONAS:PACIENTES:APP-APF:EDITAR';

-- =====================================================
--     NURSES - ÁREA 9: NOMENCLADORES (Solo lectura)
-- =====================================================

-- Medicamentos (para verificar administración)
INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'NURSES', id FROM identity.permission
WHERE code = 'NOMENCLADORES:MEDICAMENTOS:MENU';

-- Exámenes disponibles
INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'NURSES', id FROM identity.permission
WHERE code = 'NOMENCLADORES:EXAMENES:MENU';

-- Vacunas (catálogo)
INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'NURSES', id FROM identity.permission
WHERE code = 'NOMENCLADORES:VACUNAS:MENU';

-- =====================================================
--     NURSES - ÁREA 10: INICIO / DASHBOARD
-- =====================================================

-- Página de inicio
INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'NURSES', id FROM identity.permission
WHERE code = 'INICIO:INICIO';

-- Plan de trabajo del día
INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'NURSES', id FROM identity.permission
WHERE code = 'INICIO:PLAN-TRABAJO';


-- #############################################################################
-- #############################################################################
-- #############################################################################
--                         PERMISOS PARA ASSISTANTS
--         (Personal administrativo: recepción, coordinación, etc.)
-- #############################################################################
-- #############################################################################
-- #############################################################################

-- Clear existing ASSISTANTS permissions
DELETE FROM identity.user_type_permission WHERE user_type = 'ASSISTANTS';

-- =====================================================
--     ASSISTANTS - ÁREA 5: ADMISIÓN (Principal)
-- =====================================================

-- Acceso al módulo de admisión
INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'ASSISTANTS', id FROM identity.permission
WHERE code = 'ATENCION MEDICA:ADMISION:MENU';

-- =====================================================
--     ASSISTANTS - ÁREA 6: CALENDARIO Y CITAS (Principal)
-- =====================================================

-- Gestión de calendario
INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'ASSISTANTS', id FROM identity.permission
WHERE code = 'CALENDARIO:GESTION-CALENDARIO';

-- Gestión de citas
INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'ASSISTANTS', id FROM identity.permission
WHERE code = 'CALENDARIO:GESTION-CITAS';

-- Ver consultas médicas (para agendar)
INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'ASSISTANTS', id FROM identity.permission
WHERE code = 'CALENDARIO:GESTION-CONSULTA-MEDICA';

-- =====================================================
--     ASSISTANTS - ÁREA 8: GESTIÓN DE PACIENTES
-- =====================================================

-- Gestión general de pacientes (crear, editar datos básicos)
INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'ASSISTANTS', id FROM identity.permission
WHERE code = 'PACIENTE:GESTION-PACIENTES';

-- Aseguradoras del paciente
INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'ASSISTANTS', id FROM identity.permission
WHERE code = 'PERSONAS:PACIENTES:ASEGURADORAS:CREAR';

INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'ASSISTANTS', id FROM identity.permission
WHERE code = 'PERSONAS:PACIENTES:ASEGURADORAS:EDITAR';

-- =====================================================
--     ASSISTANTS - ÁREA 9: NOMENCLADORES (Solo lectura)
-- =====================================================

-- Servicios (para informar a pacientes)
INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'ASSISTANTS', id FROM identity.permission
WHERE code = 'NOMENCLADORES:SERVICIO:MENU';

-- Tipos de servicio
INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'ASSISTANTS', id FROM identity.permission
WHERE code = 'NOMENCLADORES:TIPO-DE-SERVICIO:MENU';

-- Aseguradoras (referencia para registro)
INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'ASSISTANTS', id FROM identity.permission
WHERE code = 'NOMENCLADORES:ASEGURADORA:MENU';

-- =====================================================
--     ASSISTANTS - ÁREA 10: INICIO / DASHBOARD
-- =====================================================

-- Página de inicio
INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'ASSISTANTS', id FROM identity.permission
WHERE code = 'INICIO:INICIO';

-- Plan de trabajo del día
INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'ASSISTANTS', id FROM identity.permission
WHERE code = 'INICIO:PLAN-TRABAJO';


-- #############################################################################
-- #############################################################################
-- #############################################################################
--                          PERMISOS PARA PATIENTS
--              (Pacientes: acceso mínimo a su propia información)
-- #############################################################################
-- #############################################################################
-- #############################################################################

-- Clear existing PATIENTS permissions
DELETE FROM identity.user_type_permission WHERE user_type = 'PATIENTS';

-- =====================================================
--     PATIENTS - ÁREA 6: CALENDARIO Y CITAS
-- =====================================================

-- Ver sus propias citas
INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'PATIENTS', id FROM identity.permission
WHERE code = 'CALENDARIO:GESTION-CITAS';

-- =====================================================
--     PATIENTS - ÁREA 10: INICIO / DASHBOARD
-- =====================================================

-- Página de inicio
INSERT INTO identity.user_type_permission (id, user_type, permission_id)
SELECT gen_random_uuid(), 'PATIENTS', id FROM identity.permission
WHERE code = 'INICIO:INICIO';


-- #############################################################################
-- #############################################################################
--                              VERIFICACIÓN
-- #############################################################################
-- #############################################################################

-- =====================================================
-- VERIFICACIÓN: Descomentar para ver permisos asignados
-- =====================================================
-- SELECT utp.user_type, COUNT(*) as total_permissions
-- FROM identity.user_type_permission utp
-- WHERE utp.user_type IN ('DOCTORS', 'NURSES', 'ASSISTANTS', 'PATIENTS')
-- GROUP BY utp.user_type
-- ORDER BY utp.user_type;

-- SELECT utp.user_type, p.code, p.description
-- FROM identity.user_type_permission utp
-- JOIN identity.permission p ON utp.permission_id = p.id
-- WHERE utp.user_type IN ('DOCTORS', 'NURSES', 'ASSISTANTS', 'PATIENTS')
-- ORDER BY utp.user_type, p.code;
