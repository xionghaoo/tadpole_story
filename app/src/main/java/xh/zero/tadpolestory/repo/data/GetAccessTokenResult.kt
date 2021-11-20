package xh.zero.tadpolestory.repo.data

class GetAccessTokenResult : BaseData() {
    /**
     * access_token : 37e1476146959af89ea4ff4dedb2264e
     * refresh_token : fc9185585eee7a007cc7de85317c6be1
     * uid : 299840326
     * device_id : 11780_00_100780_OSR1.180418.026
     * scope : play_history:write,subscribe:write,play_history:read,xxm_user,subscribe:read,open_pay:read,xiaoyavip,profile:read
     * expires_in : 7200
     * token :
     */
    var access_token: String? = null
    var refresh_token: String? = null
    var uid = 0
    var device_id: String? = null
    var scope: String? = null
    var expires_in = 0
    var token: String? = null
}