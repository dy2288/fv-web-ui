package ca.firstvoices.characters;

import ca.firstvoices.maintenance.common.CommonConstants;
import ca.firstvoices.maintenance.listeners.ManageRequiredJobsListener;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.test.CapturingEventListener;

public class CharactersTestUtils {
  private CharactersTestUtils() {
    throw new IllegalStateException("Utility class");
  }

  public static Event getAddToRequiredJobsEvent(CapturingEventListener capturingEvents) {
    List<Event> firedEvents = capturingEvents.getCapturedEvents();
    Event addToRequiredJobsEvent = firedEvents.stream().findFirst().filter(
        e -> CommonConstants.ADD_TO_REQUIRED_JOBS_EVENT_ID.equals(e.getName())
    ).orElse(null);

    return addToRequiredJobsEvent;
  }

  public static boolean requiredJobFired(CapturingEventListener capturingEventsListener, String requiredJob) {
    // Capture subsequent fired `addToRequiredJobs` events
    Event addToRequiredJobsEvent = CharactersTestUtils.getAddToRequiredJobsEvent(capturingEventsListener);

    if (addToRequiredJobsEvent == null) {
      return false;
    }

    @SuppressWarnings("unchecked")
    Set<String> jobs = (HashSet<String>) addToRequiredJobsEvent.getContext().getProperties().get(
        ManageRequiredJobsListener.JOB_IDS_PROP);

    return jobs.contains(requiredJob);
  }

}
