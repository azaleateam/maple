package team.azalea.maple.punishments

import team.azalea.maple.Database
import team.azalea.maple.util.formatDateWithSeconds
import java.net.URLEncoder
import java.time.Instant

private const val SUPPORT_FORM_URL = "https://support.minehut.com/hc/en-us/requests/new"

private const val SUBJECT_FIELD = "tf_subject"
private const val DESCRIPTION_FIELD = "tf_description"
private const val CATEGORY_FIELD = "tf_27062997154195"
private const val REPORT_TYPE_FIELD = "tf_27063229498259"
private const val MODERATOR_USERNAME_FIELD = "tf_27063020886291"
private const val REPORTED_USERNAME_FIELD = "tf_31915142821523"
private const val SERVER_NAME_FIELD = "tf_27062989544851"
private const val SERVER_ID_FIELD = "tf_27063041501203"

suspend fun getMinehutReportLink(punishment: Punishment, timestamp: Instant): String {
    val shortReason = getReasonInfo(punishment.reason).first
    val formattedTimestamp = formatDateWithSeconds(timestamp)
    val minehutInfo = Database.getMinehutInfo()

    val parameters = mutableMapOf(
        SUBJECT_FIELD to "Reporting Player",
        CATEGORY_FIELD to "reports_appeals",
        REPORT_TYPE_FIELD to "report_user",
        MODERATOR_USERNAME_FIELD to punishment.moderator.name,
        REPORTED_USERNAME_FIELD to punishment.player.name,
        DESCRIPTION_FIELD to """
            This player is being reported for ${shortReason.lowercase()}.
            They have already been punished on our server at $formattedTimestamp UTC.
        """.trimIndent(),
        SERVER_NAME_FIELD to minehutInfo.first,
        SERVER_ID_FIELD to minehutInfo.second,
    )

    val queryBuilder = StringBuilder("?")
    parameters.forEach { (key, value) ->
        if (queryBuilder.last() != '?') {
            queryBuilder.append("&")
        }
        val encodedValue = URLEncoder.encode(value, Charsets.UTF_8)
        queryBuilder.append("$key=$encodedValue")
    }

    return SUPPORT_FORM_URL + queryBuilder.toString()
}