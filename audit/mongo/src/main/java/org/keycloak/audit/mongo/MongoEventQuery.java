package org.keycloak.audit.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import org.keycloak.audit.Event;
import org.keycloak.audit.EventQuery;
import org.keycloak.audit.EventType;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class MongoEventQuery implements EventQuery {

    private Integer firstResult;
    private Integer maxResults;
    private DBCollection audit;
    private final BasicDBObject query;

    public MongoEventQuery(DBCollection audit) {
        this.audit = audit;
        query = new BasicDBObject();
    }

    @Override
    public EventQuery event(EventType... events) {
        List<String> eventStrings = new LinkedList<String>();
        for (EventType e : events) {
            eventStrings.add(e.toString());
        }
        query.put("event", new BasicDBObject("$in", eventStrings));
        return this;
    }

    @Override
    public EventQuery realm(String realmId) {
        query.put("realmId", realmId);
        return this;
    }

    @Override
    public EventQuery client(String clientId) {
        query.put("clientId", clientId);
        return this;
    }

    @Override
    public EventQuery user(String userId) {
        query.put("userId", userId);
        return this;
    }

    @Override
    public EventQuery ipAddress(String ipAddress) {
        query.put("ipAddress", ipAddress);
        return this;
    }

    @Override
    public EventQuery firstResult(int firstResult) {
        this.firstResult = firstResult;
        return this;
    }

    @Override
    public EventQuery maxResults(int maxResults) {
        this.maxResults = maxResults;
        return this;
    }

    @Override
    public List<Event> getResultList() {
        DBCursor cur = audit.find(query).sort(new BasicDBObject("time", -1));
        if (firstResult != null) {
            cur.skip(firstResult);
        }
        if (maxResults != null) {
            cur.limit(maxResults);
        }

        List<Event> events = new LinkedList<Event>();
        while (cur.hasNext()) {
            events.add(MongoAuditProvider.convert((BasicDBObject) cur.next()));
        }

        return events;
    }

}
