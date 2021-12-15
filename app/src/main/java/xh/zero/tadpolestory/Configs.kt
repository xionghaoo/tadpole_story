package xh.zero.tadpolestory


class Configs {
    companion object {
        const val MQTT_SERVER_URI = "tcp://broker.emqx.io:1883"

        const val HOST = BuildConfig.DOMAIN_RELEASE
        const val TADPOLE_HOST = BuildConfig.TADPOLE_HOST

        const val PACKAGE_NAME = BuildConfig.APPLICATION_ID

        const val PAGE_SIZE = 20

        const val XIMALAYA_APP_KEY = "bbb23ef40a80434386838220a342a1b4"
        const val XIMALAYA_APP_SECRET = "C459397E1F4308BA84CC287DFEE1FD4F"
        const val XIMALAYA_SN = "11780_00_100780"

        // 6: 儿童，92: 少儿教育
        const val CATEGORY_ID_STORY = 6
        const val CATEGORY_ID_LITERACY = 92

        const val DB_VERSION = BuildConfig.VERSION_CODE
//        const val CATEGORY_ID = 92

        const val ubtAppId = "980010196" // 980010196 940010010
        const val ubtProduct = "90101"
        const val ubtAppKey = "7c992a8097874cfdacbe71feda7cb8b8"
    }

}