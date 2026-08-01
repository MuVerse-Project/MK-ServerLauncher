package me.mucloud.application.mk.serverlauncher.mucore

open class MuResult<T>(
    val isOk: Boolean,
    val value: T? = null,
    val msg: String? = null,
)

class MuStateResult(
    isOk: Boolean,
    msg: String? = null,
): MuResult<Nothing>(isOk = isOk, msg = msg){
    companion object {
        val OK = MuStateResult(true)
    }
}