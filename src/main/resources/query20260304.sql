SELECT
    al.idAlumnos,
    al.nombre,
    al.apellidoPaterno,
    al.apellidoMaterno,
    al.telefono,
    ga.idGrupo,
    gru.diaSemana,
    gru.horaInicio,
    m.nombre AS modalidad,
    alm.colegiatura AS monto,
    COALESCE(MAX(CASE WHEN la.status = 'P' THEN la.numeroSemana ELSE NULL END), 0) + 1 AS numeroSemanaSiguiente,
    (SELECT COALESCE(MAX(CAST(folio AS UNSIGNED)) + 1, 1) FROM iteci.listaasistencia) AS folio,
    ga.idGrupoAlumnos,
    gru.horaFin,
    SUM(CASE WHEN la.fechaClase < CURDATE() AND la.folio IS NULL THEN 1 ELSE 0 END) AS totalObservaciones,
    SUM(CASE WHEN la.fechaClase > CURDATE() AND la.folio IS NOT NULL THEN 1 ELSE 0 END) AS adelantadas,
    COALESCE(MAX(CASE WHEN la.status = 'P' AND la.fechaClase <= CURDATE() THEN la.numeroSemana ELSE NULL END), 0) + 1 AS semanaActualPago
FROM iteci.alumnos AS al
JOIN iteci.`grupo-alumnos` AS ga ON al.idAlumnos = ga.idAlumno
JOIN iteci.grupos AS gru ON gru.idGrupo = ga.idGrupo
JOIN iteci.modalidades AS m ON gru.idModalidad = m.idModalidad
JOIN iteci.`alumnos-modalidades` AS alm ON alm.idAlumno = al.idAlumnos
LEFT JOIN iteci.listaasistencia AS la ON la.idGrupoAlumno = ga.idGrupoAlumnos
WHERE al.idAlumnos = 46
GROUP BY
    ga.idGrupoAlumnos,
    al.idAlumnos,
    al.nombre,
    al.apellidoPaterno,
    al.apellidoMaterno,
    al.telefono,
    ga.idGrupo,
    gru.diaSemana,
    gru.horaInicio,
    gru.horaFin,
    m.nombre,
    alm.colegiatura;