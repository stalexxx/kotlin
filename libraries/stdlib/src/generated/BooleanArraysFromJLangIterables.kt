// NOTE this file is auto-generated from src/kotlin/JLangIterables.kt
package kotlin

import kotlin.util.*

import java.util.*

/**
 * Returns *true* if all elements match the given *predicate*
 *
 * @includeFunctionBody ../../test/CollectionTest.kt all
 */
public inline fun BooleanArray.all(predicate: (Boolean) -> Boolean) : Boolean {
    for (element in this) if (!predicate(element)) return false
    return true
}

/**
 * Returns *true* if any elements match the given *predicate*
 *
 * @includeFunctionBody ../../test/CollectionTest.kt any
 */
public inline fun BooleanArray.any(predicate: (Boolean) -> Boolean) : Boolean {
    for (element in this) if (predicate(element)) return true
    return false
}

/**
 * Appends the string from all the elements separated using the *separator* and using the given *prefix* and *postfix* if supplied
 *
 * If a collection could be huge you can specify a non-negative value of *limit* which will only show a subset of the collection then it will
 * a special *truncated* separator (which defaults to "..."
 *
 * @includeFunctionBody ../../test/CollectionTest.kt appendString
 */
public inline fun BooleanArray.appendString(buffer: Appendable, separator: String = ", ", prefix: String = "", postfix: String = "", limit: Int = -1, truncated: String = "..."): Unit {
    buffer.append(prefix)
    var count = 0
    for (element in this) {
        if (++count > 1) buffer.append(separator)
        if (limit < 0 || count <= limit) {
            val text = if (element == null) "null" else element.toString()
            buffer.append(text)
        } else break
    }
    if (limit >= 0 && count > limit) buffer.append(truncated)
    buffer.append(postfix)
}

/**
 * Returns the number of elements which match the given *predicate*
 *
 * @includeFunctionBody ../../test/CollectionTest.kt count
 */
public inline fun BooleanArray.count(predicate: (Boolean) -> Boolean) : Int {
    var count = 0
    for (element in this) if (predicate(element)) count++
    return count
}

/**
 * Returns the first element which matches the given *predicate* or *null* if none matched
 *
 * @includeFunctionBody ../../test/CollectionTest.kt find
 */
public inline fun BooleanArray.find(predicate: (Boolean) -> Boolean) : Boolean? {
    for (element in this) if (predicate(element)) return element
    return null
}

/**
 * Filters all elements which match the given predicate into the given list
 *
 * @includeFunctionBody ../../test/CollectionTest.kt filterIntoLinkedList
 */
public inline fun <C: Collection<Boolean>> BooleanArray.filterTo(result: C, predicate: (Boolean) -> Boolean) : C {
    for (element in this) if (predicate(element)) result.add(element)
    return result
}

/**
 * Returns a list containing all elements which do not match the given *predicate*
 *
 * @includeFunctionBody ../../test/CollectionTest.kt filterNotIntoLinkedList
 */
public inline fun <L: List<Boolean>> BooleanArray.filterNotTo(result: L, predicate: (Boolean) -> Boolean) : L {
    for (element in this) if (!predicate(element)) result.add(element)
    return result
}

/**
 * Filters all non-*null* elements into the given list
 *
 * @includeFunctionBody ../../test/CollectionTest.kt filterNotNullIntoLinkedList
 */
public inline fun <L: List<Boolean>> BooleanArray?.filterNotNullTo(result: L) : L {
    if (this != null) {
        for (element in this) if (element != null) result.add(element)
    }
    return result
}

/**
 * Returns the result of transforming each element to one or more values which are concatenated together into a single list
 *
 * @includeFunctionBody ../../test/CollectionTest.kt flatMap
 */
public inline fun <R> BooleanArray.flatMapTo(result: Collection<R>, transform: (Boolean) -> Collection<R>) : Collection<R> {
    for (element in this) {
        val list = transform(element)
        if (list != null) {
            for (r in list) result.add(r)
        }
    }
    return result
}

/**
 * Performs the given *operation* on each element
 *
 * @includeFunctionBody ../../test/CollectionTest.kt forEach
 */
public inline fun BooleanArray.forEach(operation: (Boolean) -> Unit) : Unit = for (element in this) operation(element)

/**
 * Folds all elements from from left to right with the *initial* value to perform the operation on sequential pairs of elements
 *
 * @includeFunctionBody ../../test/CollectionTest.kt fold
 */
public inline fun BooleanArray.fold(initial: Boolean, operation: (Boolean, Boolean) -> Boolean): Boolean {
    var answer = initial
    for (element in this) answer = operation(answer, element)
    return answer
}

/**
 * Folds all elements from right to left with the *initial* value to perform the operation on sequential pairs of elements
 *
 * @includeFunctionBody ../../test/CollectionTest.kt foldRight
 */
public inline fun BooleanArray.foldRight(initial: Boolean, operation: (Boolean, Boolean) -> Boolean): Boolean = reverse().fold(initial, operation)

/**
 * Groups the elements in the collection into a new [[Map]] using the supplied *toKey* function to calculate the key to group the elements by
 *
 * @includeFunctionBody ../../test/CollectionTest.kt groupBy
 */
public inline fun <K> BooleanArray.groupBy(toKey: (Boolean) -> K) : Map<K, List<Boolean>> = groupByTo<K>(HashMap<K, List<Boolean>>(), toKey)

/**
 * Groups the elements in the collection into the given [[Map]] using the supplied *toKey* function to calculate the key to group the elements by
 *
 * @includeFunctionBody ../../test/CollectionTest.kt groupBy
 */
public inline fun <K> BooleanArray.groupByTo(result: Map<K, List<Boolean>>, toKey: (Boolean) -> K) : Map<K, List<Boolean>> {
    for (element in this) {
        val key = toKey(element)
        val list = result.getOrPut(key) { ArrayList<Boolean>() }
        list.add(element)
    }
    return result
}

/**
 * Creates a string from all the elements separated using the *separator* and using the given *prefix* and *postfix* if supplied.
 *
 * If a collection could be huge you can specify a non-negative value of *limit* which will only show a subset of the collection then it will
 * a special *truncated* separator (which defaults to "..."
 *
 * @includeFunctionBody ../../test/CollectionTest.kt makeString
 */
public inline fun BooleanArray.makeString(separator: String = ", ", prefix: String = "", postfix: String = "", limit: Int = -1, truncated: String = "..."): String {
    val buffer = StringBuilder()
    appendString(buffer, separator, prefix, postfix, limit, truncated)
    return buffer.toString().sure()
}

/** Returns a list containing the first elements that satisfy the given *predicate* */
public inline fun <L: List<Boolean>> BooleanArray.takeWhileTo(result: L, predicate: (Boolean) -> Boolean) : L {
    for (element in this) if (predicate(element)) result.add(element) else break
    return result
}

/**
 * Reverses the order the elements into a list
 *
 * @includeFunctionBody ../../test/CollectionTest.kt reverse
 */
public inline fun BooleanArray.reverse() : List<Boolean> {
    val answer = LinkedList<Boolean>()
    for (element in this) answer.addFirst(element)
    return answer
}

/** Copies all elements into the given collection */
public inline fun <C: Collection<Boolean>> BooleanArray.toCollection(result: C) : C {
    for (element in this) result.add(element)
    return result
}

/** Copies all elements into a [[LinkedList]]  */
public inline fun  BooleanArray.toLinkedList() : LinkedList<Boolean> = toCollection(LinkedList<Boolean>())

/** Copies all elements into a [[List]] */
public inline fun  BooleanArray.toList() : List<Boolean> = toCollection(ArrayList<Boolean>())

/** Copies all elements into a [[List] */
public inline fun  BooleanArray.toCollection() : Collection<Boolean> = toCollection(ArrayList<Boolean>())

/** Copies all elements into a [[Set]] */
public inline fun  BooleanArray.toSet() : Set<Boolean> = toCollection(HashSet<Boolean>())

/** Copies all elements into a [[SortedSet]] */
public inline fun  BooleanArray.toSortedSet() : SortedSet<Boolean> = toCollection(TreeSet<Boolean>())

/**
  TODO figure out necessary variance/generics ninja stuff... :)
public inline fun  BooleanArray.toSortedList(transform: fun(Boolean) : java.lang.Comparable<*>) : List<Boolean> {
    val answer = this.toList()
    answer.sort(transform)
    return answer
}
*/
