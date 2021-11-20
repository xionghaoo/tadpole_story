package xh.zero.tadpolestory.repo.data

abstract class BaseData {
    val code: Int = -1
    // 200 时的信息
    val msg: String? = null

    /**
     * 203: 认证码过期
     */
    val error_no: Int = -1
    val error_desc: String? = null
}