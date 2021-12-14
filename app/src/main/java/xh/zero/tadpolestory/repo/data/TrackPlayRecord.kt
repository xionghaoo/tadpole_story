package xh.zero.tadpolestory.repo.data

data class TrackPlayRecord(
    // 声音 ID
    val track_id: Int,
    // 专辑 ID
    val album_id: Int,
    // 播放时长，即本次播放总共播放了多长时间，单位为秒
    val duration: Int,
    // 播放到第几秒或最后播放到的位置，是相对于这个音频开始位置的一个值
    val played_secs: Int,
    // 播放开始时刻，Unix 毫秒数时间戳
    val started_at: Int,
    // 播放类型:0-联网播放，1-断网播放
    val play_type: Int = 0,
    // 1-助眠解压 2-场景一键听 其余不需要传
    val business_type: Int? = null,
    // 当 business_type=2 时必传，表示频道 id，其余不需要传
    val channel_id: Int? = null,
    // 场景 ID，business_type=2 时(场景一键听)传，其余时候不传
    val scene_id: Int? = null,
    // 主题 ID，business_type=1 时(助眠解压)传，其余时候不传
    val topic_id: Int? = null,
)