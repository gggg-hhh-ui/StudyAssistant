package com.studyassistant.domain.usecase

import com.studyassistant.domain.model.Difficulty
import com.studyassistant.domain.model.GradeLevel
import com.studyassistant.domain.model.MathProblem
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * 数学题目生成器
 * 支持按年级生成不同难度的数学题，完全离线运行
 */
class MathProblemGenerator(
    private val startGrade: Int = 1,
    private val endGrade: Int = 12
) {
    private val random = Random.Default

    init {
        require(startGrade in 1..12) { "startGrade must be 1-12" }
        require(endGrade in startGrade..12) { "endGrade must be >= startGrade" }
    }

    /**
     * 根据年级范围生成随机题目
     */
    fun generate(): MathProblem {
        val targetGrade = random.nextInt(startGrade, endGrade + 1)
        return generateForGrade(targetGrade)
    }

    /**
     * 为指定年级生成题目
     */
    fun generateForGrade(grade: Int): MathProblem {
        return when (grade) {
            1 -> generateGrade1()
            2 -> generateGrade2()
            3 -> generateGrade3()
            4 -> generateGrade4()
            5 -> generateGrade5()
            6 -> generateGrade6()
            7 -> generateGrade7()
            8 -> generateGrade8()
            9 -> generateGrade9()
            10 -> generateGrade10()
            11 -> generateGrade11()
            12 -> generateGrade12()
            else -> generateGrade3()
        }
    }

    // ==================== 小学低年级（1-2年级） ====================

    /**
     * 一年级：10以内加减法
     */
    private fun generateGrade1(): MathProblem {
        return when (random.nextInt(4)) {
            0 -> {
                val a = random.nextInt(1, 6)
                val b = random.nextInt(1, 6)
                MathProblem(
                    question = "$a + $b = ?",
                    answer = a + b,
                    difficulty = Difficulty.EASY,
                    grade = 1
                )
            }
            1 -> {
                val a = random.nextInt(3, 10)
                val b = random.nextInt(1, a)
                MathProblem(
                    question = "$a - $b = ?",
                    answer = a - b,
                    difficulty = Difficulty.EASY,
                    grade = 1
                )
            }
            2 -> {
                val a = random.nextInt(1, 11)
                val b = random.nextInt(1, 6)
                MathProblem(
                    question = "$a + $b = ?",
                    answer = a + b,
                    difficulty = Difficulty.EASY,
                    grade = 1
                )
            }
            else -> {
                val a = random.nextInt(5, 11)
                val b = random.nextInt(1, a)
                MathProblem(
                    question = "$a - $b = ?",
                    answer = a - b,
                    difficulty = Difficulty.EASY,
                    grade = 1
                )
            }
        }
    }

    /**
     * 二年级：20以内加减法
     */
    private fun generateGrade2(): MathProblem {
        return when (random.nextInt(4)) {
            0 -> {
                val a = random.nextInt(5, 15)
                val b = random.nextInt(1, 15 - a + 1)
                MathProblem(
                    question = "$a + $b = ?",
                    answer = a + b,
                    difficulty = Difficulty.EASY,
                    grade = 2
                )
            }
            1 -> {
                val a = random.nextInt(10, 20)
                val b = random.nextInt(1, a)
                MathProblem(
                    question = "$a - $b = ?",
                    answer = a - b,
                    difficulty = Difficulty.EASY,
                    grade = 2
                )
            }
            2 -> {
                val a = random.nextInt(10, 20)
                val b = random.nextInt(10, 20)
                MathProblem(
                    question = "$a + $b = ?",
                    answer = a + b,
                    difficulty = Difficulty.EASY,
                    grade = 2
                )
            }
            else -> {
                val a = random.nextInt(15, 20)
                val b = random.nextInt(5, a)
                MathProblem(
                    question = "$a - $b = ?",
                    answer = a - b,
                    difficulty = Difficulty.EASY,
                    grade = 2
                )
            }
        }
    }

    // ==================== 小学中年级（3-4年级） ====================

    /**
     * 三年级：加减乘除混合
     */
    private fun generateGrade3(): MathProblem {
        return when (random.nextInt(5)) {
            0 -> {
                val a = random.nextInt(20, 100)
                val b = random.nextInt(10, 50)
                MathProblem(
                    question = "$a + $b = ?",
                    answer = a + b,
                    difficulty = Difficulty.EASY,
                    grade = 3
                )
            }
            1 -> {
                val a = random.nextInt(50, 100)
                val b = random.nextInt(10, a)
                MathProblem(
                    question = "$a - $b = ?",
                    answer = a - b,
                    difficulty = Difficulty.EASY,
                    grade = 3
                )
            }
            2 -> {
                val a = random.nextInt(2, 10)
                val b = random.nextInt(2, 10)
                MathProblem(
                    question = "$a × $b = ?",
                    answer = a * b,
                    difficulty = Difficulty.MEDIUM,
                    grade = 3
                )
            }
            3 -> {
                val b = random.nextInt(2, 10)
                val answer = random.nextInt(2, 10)
                val a = b * answer
                MathProblem(
                    question = "$a ÷ $b = ?",
                    answer = answer,
                    difficulty = Difficulty.MEDIUM,
                    grade = 3
                )
            }
            else -> {
                val a = random.nextInt(2, 9)
                val b = random.nextInt(2, 9)
                val c = random.nextInt(1, 10)
                MathProblem(
                    question = "$a × $b + $c = ?",
                    answer = a * b + c,
                    difficulty = Difficulty.MEDIUM,
                    grade = 3
                )
            }
        }
    }

    /**
     * 四年级：三位数运算
     */
    private fun generateGrade4(): MathProblem {
        return when (random.nextInt(5)) {
            0 -> {
                val a = random.nextInt(100, 500)
                val b = random.nextInt(100, 500)
                MathProblem(
                    question = "$a + $b = ?",
                    answer = a + b,
                    difficulty = Difficulty.MEDIUM,
                    grade = 4
                )
            }
            1 -> {
                val a = random.nextInt(300, 800)
                val b = random.nextInt(100, a)
                MathProblem(
                    question = "$a - $b = ?",
                    answer = a - b,
                    difficulty = Difficulty.MEDIUM,
                    grade = 4
                )
            }
            2 -> {
                val a = random.nextInt(10, 99)
                val b = random.nextInt(2, 9)
                MathProblem(
                    question = "$a × $b = ?",
                    answer = a * b,
                    difficulty = Difficulty.MEDIUM,
                    grade = 4
                )
            }
            3 -> {
                val b = random.nextInt(10, 20)
                val answer = random.nextInt(5, 20)
                val a = b * answer
                MathProblem(
                    question = "$a ÷ $b = ?",
                    answer = answer,
                    difficulty = Difficulty.MEDIUM,
                    grade = 4
                )
            }
            else -> {
                val a = random.nextInt(10, 50)
                val b = random.nextInt(2, 9)
                val c = random.nextInt(10, 50)
                MathProblem(
                    question = "$a × $b + $c = ?",
                    answer = a * b + c,
                    difficulty = Difficulty.MEDIUM,
                    grade = 4
                )
            }
        }
    }

    // ==================== 小学高年级（5-6年级） ====================

    /**
     * 五年级：小数与分数
     */
    private fun generateGrade5(): MathProblem {
        return when (random.nextInt(5)) {
            0 -> {
                val a = random.nextInt(10, 100)
                val b = random.nextInt(10, 100)
                MathProblem(
                    question = "$a × $b = ?",
                    answer = a * b,
                    difficulty = Difficulty.MEDIUM,
                    grade = 5
                )
            }
            1 -> {
                val b = random.nextInt(10, 30)
                val answer = random.nextInt(5, 30)
                val a = b * answer
                MathProblem(
                    question = "$a ÷ $b = ?",
                    answer = answer,
                    difficulty = Difficulty.MEDIUM,
                    grade = 5
                )
            }
            2 -> {
                val a = random.nextInt(1, 20)
                MathProblem(
                    question = "${a}² = ?",
                    answer = a * a,
                    difficulty = Difficulty.MEDIUM,
                    grade = 5
                )
            }
            3 -> {
                val total = random.nextInt(10, 100) * 10
                val percent = random.nextInt(10, 50)
                MathProblem(
                    question = "$total 的 $percent% = ?",
                    answer = total * percent / 100,
                    difficulty = Difficulty.MEDIUM,
                    grade = 5
                )
            }
            else -> {
                val a = random.nextInt(20, 100)
                val b = random.nextInt(2, 9)
                val c = random.nextInt(20, 100)
                MathProblem(
                    question = "$a + $b × ${c / 10} = ?",
                    answer = a + b * (c / 10),
                    difficulty = Difficulty.MEDIUM,
                    grade = 5
                )
            }
        }
    }

    /**
     * 六年级：百分数与几何
     */
    private fun generateGrade6(): MathProblem {
        return when (random.nextInt(5)) {
            0 -> {
                val total = random.nextInt(10, 100) * 10
                val percent = random.nextInt(10, 100)
                MathProblem(
                    question = "$total 的 $percent% = ?",
                    answer = total * percent / 100,
                    difficulty = Difficulty.MEDIUM,
                    grade = 6
                )
            }
            1 -> {
                val a = random.nextInt(5, 20)
                MathProblem(
                    question = "${a}² = ?",
                    answer = a * a,
                    difficulty = Difficulty.MEDIUM,
                    grade = 6
                )
            }
            2 -> {
                val a = random.nextInt(2, 15)
                val b = random.nextInt(2, 15)
                MathProblem(
                    question = "长${a}米宽${b}米,周长=?(米)",
                    answer = 2 * (a + b),
                    difficulty = Difficulty.MEDIUM,
                    grade = 6
                )
            }
            3 -> {
                val r = random.nextInt(3, 10)
                MathProblem(
                    question = "半径${r}厘米,面积≈?(π=3)",
                    answer = 3 * r * r,
                    difficulty = Difficulty.MEDIUM,
                    grade = 6
                )
            }
            else -> {
                val a = random.nextInt(10, 100)
                val b = random.nextInt(10, 100)
                MathProblem(
                    question = "$a + $b = ?",
                    answer = a + b,
                    difficulty = Difficulty.MEDIUM,
                    grade = 6
                )
            }
        }
    }

    // ==================== 初中（7-9年级） ====================

    /**
     * 七年级：有理数与方程
     */
    private fun generateGrade7(): MathProblem {
        return when (random.nextInt(6)) {
            0 -> {
                val a = random.nextInt(10, 100)
                val b = random.nextInt(10, 100)
                MathProblem(
                    question = "(-$a) + $b = ?",
                    answer = b - a,
                    difficulty = Difficulty.MEDIUM,
                    grade = 7
                )
            }
            1 -> {
                val a = random.nextInt(2, 10)
                val x = random.nextInt(2, 20)
                val c = a * x + random.nextInt(-20, 20)
                MathProblem(
                    question = "$a x + $c = ${a * x}, x = ?",
                    answer = x,
                    difficulty = Difficulty.MEDIUM,
                    grade = 7
                )
            }
            2 -> {
                val a = random.nextInt(5, 15)
                MathProblem(
                    question = "${a}² = ?",
                    answer = a * a,
                    difficulty = Difficulty.MEDIUM,
                    grade = 7
                )
            }
            3 -> {
                val a = random.nextInt(2, 13)
                MathProblem(
                    question = "√${a * a} = ?",
                    answer = a,
                    difficulty = Difficulty.MEDIUM,
                    grade = 7
                )
            }
            4 -> {
                val a = random.nextInt(2, 8)
                MathProblem(
                    question = "${a}³ = ?",
                    answer = a * a * a,
                    difficulty = Difficulty.HARD,
                    grade = 7
                )
            }
            else -> {
                val a = random.nextInt(20, 100)
                val b = random.nextInt(10, 100)
                MathProblem(
                    question = "$a + $b = ?",
                    answer = a + b,
                    difficulty = Difficulty.EASY,
                    grade = 7
                )
            }
        }
    }

    /**
     * 八年级：一次函数与全等
     */
    private fun generateGrade8(): MathProblem {
        return when (random.nextInt(6)) {
            0 -> {
                val a = random.nextInt(2, 8)
                val b = random.nextInt(-10, 20)
                val x = random.nextInt(1, 10)
                MathProblem(
                    question = "y=${a}x+$b, x=${x}时,y=?",
                    answer = a * x + b,
                    difficulty = Difficulty.MEDIUM,
                    grade = 8
                )
            }
            1 -> {
                val a = random.nextInt(3, 10)
                MathProblem(
                    question = "${a}² = ?",
                    answer = a * a,
                    difficulty = Difficulty.MEDIUM,
                    grade = 8
                )
            }
            2 -> {
                val a = random.nextInt(2, 12)
                MathProblem(
                    question = "√${a * a} = ?",
                    answer = a,
                    difficulty = Difficulty.MEDIUM,
                    grade = 8
                )
            }
            3 -> {
                val a = random.nextInt(2, 6)
                val exp = 3
                MathProblem(
                    question = "${a}³ = ?",
                    answer = a * a * a,
                    difficulty = Difficulty.HARD,
                    grade = 8
                )
            }
            4 -> {
                val a = random.nextInt(2, 8)
                val x = random.nextInt(2, 15)
                val c = a * x + random.nextInt(1, 20)
                MathProblem(
                    question = "$a x + $c = ${a * x + c}, x = ?",
                    answer = x,
                    difficulty = Difficulty.HARD,
                    grade = 8
                )
            }
            else -> {
                val a = random.nextInt(10, 50)
                val b = random.nextInt(10, 50)
                MathProblem(
                    question = "√($a²+$b²)≈?(勾股数)",
                    answer = sqrt((a * a + b * b).toDouble()).toInt(),
                    difficulty = Difficulty.HARD,
                    grade = 8
                )
            }
        }
    }

    /**
     * 九年级：二次函数与圆
     */
    private fun generateGrade9(): MathProblem {
        return when (random.nextInt(6)) {
            0 -> {
                val a = random.nextInt(5, 12)
                MathProblem(
                    question = "${a}² = ?",
                    answer = a * a,
                    difficulty = Difficulty.MEDIUM,
                    grade = 9
                )
            }
            1 -> {
                val a = random.nextInt(5, 15)
                MathProblem(
                    question = "√${a * a} = ?",
                    answer = a,
                    difficulty = Difficulty.MEDIUM,
                    grade = 9
                )
            }
            2 -> {
                val a = random.nextInt(3, 8)
                val exp = 3
                MathProblem(
                    question = "${a}³ = ?",
                    answer = a * a * a,
                    difficulty = Difficulty.HARD,
                    grade = 9
                )
            }
            3 -> {
                val r = random.nextInt(3, 10)
                MathProblem(
                    question = "r=${r},C=2πr≈?",
                    answer = 2 * 3 * r,
                    difficulty = Difficulty.HARD,
                    grade = 9
                )
            }
            4 -> {
                val a = random.nextInt(3, 8)
                val b = random.nextInt(3, 8)
                val c = random.nextInt(3, 8)
                MathProblem(
                    question = "(${a}×${b})×${c}=?",
                    answer = a * b * c,
                    difficulty = Difficulty.HARD,
                    grade = 9
                )
            }
            else -> {
                val a = random.nextInt(10, 50)
                val b = random.nextInt(10, 50)
                val c = random.nextInt(5, 20)
                MathProblem(
                    question = "($a+$b)×$c=?",
                    answer = (a + b) * c,
                    difficulty = Difficulty.HARD,
                    grade = 9
                )
            }
        }
    }

    // ==================== 高中（10-12年级） ====================

    /**
     * 高一：集合与函数
     */
    private fun generateGrade10(): MathProblem {
        return when (random.nextInt(6)) {
            0 -> {
                val a = random.nextInt(3, 10)
                MathProblem(
                    question = "${a}² = ?",
                    answer = a * a,
                    difficulty = Difficulty.MEDIUM,
                    grade = 10
                )
            }
            1 -> {
                val a = random.nextInt(5, 15)
                MathProblem(
                    question = "√${a * a} = ?",
                    answer = a,
                    difficulty = Difficulty.MEDIUM,
                    grade = 10
                )
            }
            2 -> {
                val base = random.nextInt(2, 5)
                val exp = random.nextInt(2, 5)
                MathProblem(
                    question = "${base}^$exp = ?",
                    answer = Math.pow(base.toDouble(), exp.toDouble()).toInt(),
                    difficulty = Difficulty.HARD,
                    grade = 10
                )
            }
            3 -> {
                val a = random.nextInt(2, 8)
                val exp = 3
                MathProblem(
                    question = "${a}³ = ?",
                    answer = a * a * a,
                    difficulty = Difficulty.HARD,
                    grade = 10
                )
            }
            4 -> {
                val a = random.nextInt(2, 8)
                MathProblem(
                    question = "log₂${1 shl a}=?",
                    answer = a,
                    difficulty = Difficulty.HARD,
                    grade = 10
                )
            }
            else -> {
                val a = random.nextInt(10, 100)
                val b = random.nextInt(10, 100)
                MathProblem(
                    question = "$a + $b = ?",
                    answer = a + b,
                    difficulty = Difficulty.MEDIUM,
                    grade = 10
                )
            }
        }
    }

    /**
     * 高二：数列与解析几何
     */
    private fun generateGrade11(): MathProblem {
        return when (random.nextInt(6)) {
            0 -> {
                val a = random.nextInt(2, 8)
                val n = random.nextInt(3, 6)
                MathProblem(
                    question = "等比${a}的${n}次=?", 
                    answer = Math.pow(a.toDouble(), n.toDouble()).toInt(),
                    difficulty = Difficulty.HARD,
                    grade = 11
                )
            }
            1 -> {
                val a = random.nextInt(2, 10)
                val d = random.nextInt(2, 5)
                val n = random.nextInt(3, 8)
                MathProblem(
                    question = "等差首${a}公差${d}第${n}项=?",
                    answer = a + (n - 1) * d,
                    difficulty = Difficulty.HARD,
                    grade = 11
                )
            }
            2 -> {
                val a = random.nextInt(3, 10)
                MathProblem(
                    question = "${a}² = ?",
                    answer = a * a,
                    difficulty = Difficulty.MEDIUM,
                    grade = 11
                )
            }
            3 -> {
                val base = random.nextInt(2, 5)
                val exp = random.nextInt(2, 5)
                MathProblem(
                    question = "${base}^$exp = ?",
                    answer = Math.pow(base.toDouble(), exp.toDouble()).toInt(),
                    difficulty = Difficulty.HARD,
                    grade = 11
                )
            }
            4 -> {
                val a = random.nextInt(2, 8)
                MathProblem(
                    question = "${a}! = ?",
                    answer = factorial(a),
                    difficulty = Difficulty.HARD,
                    grade = 11
                )
            }
            else -> {
                val a = random.nextInt(5, 15)
                val b = random.nextInt(5, 15)
                MathProblem(
                    question = "√($a²+$b²)=?",
                    answer = sqrt((a * a + b * b).toDouble()).toInt(),
                    difficulty = Difficulty.HARD,
                    grade = 11
                )
            }
        }
    }

    /**
     * 高三：导数与综合
     */
    private fun generateGrade12(): MathProblem {
        return when (random.nextInt(6)) {
            0 -> {
                val a = random.nextInt(3, 8)
                MathProblem(
                    question = "${a}! = ?",
                    answer = factorial(a),
                    difficulty = Difficulty.HARD,
                    grade = 12
                )
            }
            1 -> {
                val base = random.nextInt(2, 6)
                val exp = random.nextInt(2, 5)
                MathProblem(
                    question = "${base}^$exp = ?",
                    answer = Math.pow(base.toDouble(), exp.toDouble()).toInt(),
                    difficulty = Difficulty.HARD,
                    grade = 12
                )
            }
            2 -> {
                val a = random.nextInt(5, 12)
                MathProblem(
                    question = "√${a * a} = ?",
                    answer = a,
                    difficulty = Difficulty.MEDIUM,
                    grade = 12
                )
            }
            3 -> {
                val n = random.nextInt(4, 8)
                MathProblem(
                    question = "C(${n},2)=?",
                    answer = n * (n - 1) / 2,
                    difficulty = Difficulty.HARD,
                    grade = 12
                )
            }
            4 -> {
                val a = random.nextInt(2, 10)
                val b = random.nextInt(2, 10)
                MathProblem(
                    question = "$a × $b = ?",
                    answer = a * b,
                    difficulty = Difficulty.HARD,
                    grade = 12
                )
            }
            else -> {
                val a = random.nextInt(10, 100)
                val b = random.nextInt(10, 100)
                MathProblem(
                    question = "($a+$b)÷2=?",
                    answer = (a + b) / 2,
                    difficulty = Difficulty.HARD,
                    grade = 12
                )
            }
        }
    }

    private fun factorial(n: Int): Int {
        var result = 1
        for (i in 2..n) result *= i
        return result
    }
}
