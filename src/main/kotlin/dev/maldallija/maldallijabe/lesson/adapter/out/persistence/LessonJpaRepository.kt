package dev.maldallija.maldallijabe.lesson.adapter.out.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface LessonJpaRepository : JpaRepository<LessonEntity, Long>
