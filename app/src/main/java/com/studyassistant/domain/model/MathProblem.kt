package com.studyassistant.domain.model

/**
 * 数学题目
 */
data class MathProblem(
    val question: String,       // 题目文本，如 "12 + 34 = ?"
    val answer: Int,            // 正确答案
    val difficulty: Difficulty,  // 难度等级
    val grade: Int              // 适用年级（1-12）
)

/**
 * 年级枚举（一年级到高三）
 */
enum class GradeLevel(
    val gradeNumber: Int,
    val displayName: String,
    val shortName: String
) {
    GRADE_1(1, "一年级", "小1"),
    GRADE_2(2, "二年级", "小2"),
    GRADE_3(3, "三年级", "小3"),
    GRADE_4(4, "四年级", "小4"),
    GRADE_5(5, "五年级", "小5"),
    GRADE_6(6, "六年级", "小6"),
    GRADE_7(7, "七年级", "初1"),
    GRADE_8(8, "八年级", "初2"),
    GRADE_9(9, "九年级", "初3"),
    GRADE_10(10, "高一", "高1"),
    GRADE_11(11, "高二", "高2"),
    GRADE_12(12, "高三", "高3");

    companion object {
        fun fromGrade(grade: Int): GradeLevel {
            return entries.find { it.gradeNumber == grade } ?: GRADE_3
        }

        fun fromOrdinal(ordinal: Int): GradeLevel {
            return entries.getOrElse(ordinal) { GRADE_3 }
        }
    }
}

/**
 * 难度等级
 */
enum class Difficulty(val displayName: String) {
    EASY("简单"),
    MEDIUM("中等"),
    HARD("困难")
}

/**
 * 题目类型
 */
enum class ProblemType {
    ADDITION,
    SUBTRACTION,
    MULTIPLICATION,
    DIVISION,
    MIXED
}
