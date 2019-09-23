import groovy.json.*
import groovy.xml.*
import java.time.*

int MAX_SIZE = 5

def eventsApi = 'https://api.meetup.com/seajug/events?&sign=true&photo-host=public&page=200&status=past&desc=true'
def rs = new JsonSlurper().parseText(eventsApi.toURL().text)

new MarkupBuilder().content {
    rs.take(MAX_SIZE).groupBy { toLocalDate(it.time).year }.each { group ->
        h2(group.key)
        ul {
            group.value.each { event ->
                def comment = getInfoComment(event.id)
                li {
                    a(href: event.link, "${toLocalDate(event.time)} $event.name")
                    if (comment) {
                        getInfoMap(comment).each { key, value -> a(href: value, "[$key]") }
                    }
                }
            }
        }
    }
}

private static def getInfoMap(def infoComment) {
    return infoComment.comment
            .split('\n')
            .drop(1)
            .collectEntries { entry ->
                def pair = entry.split(': ')
                [(pair.first()): pair.last()]
            }
}

private static def getInfoComment(def eventId) {
    return new JsonSlurper().parseText("https://api.meetup.com/seajug/events/$eventId/comments".toURL().text)
            .findAll { it.member.role in ['organizer', 'event_organizer'] }
            .find { it.comment.startsWith('[links]') }
}

private static LocalDate toLocalDate(long dateTime) {
    return Instant.ofEpochMilli(dateTime).atZone(ZoneId.of('America/Los_Angeles')).toLocalDate()
}
