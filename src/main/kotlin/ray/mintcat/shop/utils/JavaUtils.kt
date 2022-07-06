package ray.mintcat.shop.utils

import org.bukkit.util.NumberConversions
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.Map

fun <K, V> Map<K, V>.toHashMap(): HashMap<K, V> {
    val map = hashMapOf<K, V>()
    this.forEach {
        map[it.key] = it.value
    }
    return map
}

/**
 * JavaUtils
 * @author Ray_Hughes
 * @Time 2022/1/26
 * @since 1.0
 */

fun Collection<String>.replace(old: String, new: String): Collection<String> {
    return this.map { it.replace(old, new) }
}

/**
 * 集合拆箱操作
 */
fun <T> Collection<Collection<T>>.devanning(): Collection<T> {
    val list = mutableListOf<T>()
    this@devanning.asSequence().forEach { a ->
        a.forEach { list.add(it) }
    }
    return list
}

fun <T> Collection<T>.contains(element: Collection<T>): Boolean {
    element.forEach {
        if (this.contains(it)) {
            return true
        }
    }
    return false
}

fun Long.toTimeString(pattern: String? = "yyyy年MM月dd日HH时mm分ss秒"): String {
    return SimpleDateFormat(pattern!!).format(Date(this))
}

fun String.asChar(): Char {
    return this.toCharArray()[0]
}

fun Int.asChar(): Char {
    return this.toString().toCharArray()[0]
}

fun String.asDouble(): Double {
    return NumberConversions.toDouble(this)
}

fun Double.getTwo(): Double {
    return DecimalFormat("######0.00").format(this).asDouble()
}


fun <T> List<T>.getLists(int: Int, def: T): List<T> {
    val subs = mutableListOf<T>()
    if (this.size < int) {
        val cha = int - this.size
        (1..cha).forEach { _ ->
            subs.add(def)
        }
        subs.addAll(this)
        return subs
    } else {
        (1..int).forEach { ints ->
            subs.add(this.get(this.size - ints))
        }
        return subs.reversed()
    }
}
