package com.techyourchance.multithreading.exercises.exercise10

import androidx.annotation.WorkerThread
import kotlinx.coroutines.*
import java.math.BigInteger

class ComputeFactorialUseCase {

    sealed class Result {
        class Success(val result: BigInteger) : Result()
        object Timeout : Result()
    }

    suspend fun computeFactorial(argument: Int, timeout: Int): Result {
        return withContext(Dispatchers.IO){
            try {
                withTimeout(timeMillis = timeout.toLong()){
                    val computationRanges = initThreadsComputationRanges(argument)
                    val partialProducts = startComputation(computationRanges)
                    val result = computeFinalResult(partialProducts)
                    Result.Success(result)
                }
            }catch (exception: Exception){
                Result.Timeout
            }
        }
    }

    private fun numberOfThreads(factorialArgument: Int): Int{
        return if (factorialArgument < 20)
            1
        else
            Runtime.getRuntime().availableProcessors()
    }

    private fun initThreadsComputationRanges(factorialArgument: Int): Array<ComputationRange>{
        val numThreads = numberOfThreads(factorialArgument)
        val computationRangeSize = factorialArgument / numThreads
        val threadsComputationRanges= Array(numThreads) { ComputationRange(0, 0) }

        var nextComputationRangeEnd = factorialArgument.toLong()
        for (i in numThreads - 1 downTo 0) {
            threadsComputationRanges[i] = ComputationRange(
                    nextComputationRangeEnd - computationRangeSize + 1,
                    nextComputationRangeEnd
            )
            nextComputationRangeEnd = threadsComputationRanges[i].start - 1
        }

        // add potentially "remaining" values to first thread's range
        threadsComputationRanges[0] = ComputationRange(1, threadsComputationRanges[0].end)

        return threadsComputationRanges
    }

    private suspend fun startComputation(computationRanges: Array<ComputationRange>): List<BigInteger> = coroutineScope {
        return@coroutineScope withContext(Dispatchers.IO){
            return@withContext computationRanges.map {
                computePartialProductRangeAsync(it)
            }.awaitAll()
        }
    }

    private fun CoroutineScope.computePartialProductRangeAsync(computationRange: ComputationRange): Deferred<BigInteger> = async(Dispatchers.IO) {
        val rangeStart = computationRange.start
        val rangeEnd = computationRange.end

        var product = BigInteger("1")
        for (num in rangeStart..rangeEnd) {
            if (!isActive) {
                break
            }
            product = product.multiply(BigInteger(num.toString()))
        }

        return@async product
    }

    @WorkerThread
    private suspend fun computeFinalResult(partialProducts: List<BigInteger>): BigInteger = withContext(Dispatchers.IO) {
        var result = BigInteger("1")

        for (element in partialProducts) {
            if (!isActive) {
                break
            }
            result = result.multiply(element)
        }
        return@withContext result
    }

    private data class ComputationRange(val start: Long, val end: Long)
}
