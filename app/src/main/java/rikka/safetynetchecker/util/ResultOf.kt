package rikka.safetynetchecker.util

sealed class ResultOf<out R> {
    class Success<R>(val value: R) : ResultOf<R>()
    class Failure(val error: Throwable) : ResultOf<Nothing>()
    object Loading : ResultOf<Nothing>()
    object Initial : ResultOf<Nothing>()
}
