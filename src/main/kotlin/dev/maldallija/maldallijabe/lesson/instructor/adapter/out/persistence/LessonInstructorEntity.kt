package dev.maldallija.maldallijabe.lesson.instructor.adapter.out.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "lesson_instructor")
class LessonInstructorEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,
    @Column(name = "lesson_id", nullable = false)
    val lessonId: Long,
    @Column(name = "staff_id", nullable = false)
    val staffId: Long,
    @Column(name = "created_at", nullable = false)
    val createdAt: Instant,
)
