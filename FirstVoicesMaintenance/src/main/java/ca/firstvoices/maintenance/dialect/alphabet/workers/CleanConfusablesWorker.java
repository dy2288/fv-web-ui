package ca.firstvoices.maintenance.dialect.alphabet.workers;

import static ca.firstvoices.lifecycle.Constants.PUBLISHED_STATE;

import ca.firstvoices.characters.Constants;
import ca.firstvoices.characters.services.CleanupCharactersService;
import ca.firstvoices.maintenance.services.MaintenanceLogger;
import ca.firstvoices.publisher.services.FirstVoicesPublisherService;
import ca.firstvoices.services.UnpublishedChangesService;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.CoreInstance;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.api.repository.RepositoryManager;
import org.nuxeo.ecm.core.work.AbstractWork;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.transaction.TransactionHelper;

/**
 * Clean Confusables worker will search for words and phrases that contain confusable characters,
 * and clean them
 */
public class CleanConfusablesWorker extends AbstractWork {

  private static final String LC_CONFUSABLES = "fvcharacter:confusable_characters";
  private static final String UC_CONFUSABLES = "fvcharacter:upper_case_confusable_characters";

  private static final long serialVersionUID = 1L;

  private static final Log log = LogFactory.getLog(CleanConfusablesWorker.class);

  private final String job;

  private final DocumentRef jobContainerRef;

  private final int batchSize;

  private final transient CleanupCharactersService cleanupCharactersService = Framework
      .getService(CleanupCharactersService.class);

  public CleanConfusablesWorker(DocumentRef dialectRef, String job, int batchSize) {
    super(Constants.CLEAN_CONFUSABLES_JOB_ID);
    this.jobContainerRef = dialectRef;
    this.job = job;
    this.batchSize = batchSize;

    RepositoryManager rpm = Framework.getService(RepositoryManager.class);

    // See https://doc.nuxeo.com/nxdoc/work-and-workmanager/#work-construction
    setDocument(rpm.getDefaultRepositoryName(), jobContainerRef.toString(), true);
  }

  @Override
  public void work() {

    if (!TransactionHelper.isTransactionActive()) {
      TransactionHelper.startTransaction();
    }

    CoreInstance
        .doPrivileged(Framework.getService(RepositoryManager.class).getDefaultRepositoryName(),
            session -> {
              DocumentModel dialect = session.getDocument(jobContainerRef);
              setStatus("Cleaning confusables `" + dialect.getTitle() + "`");

              MaintenanceLogger maintenanceLogger = Framework.getService(MaintenanceLogger.class);

              try {
                DocumentModelList characters = cleanupCharactersService
                    .getCharactersWithConfusables(dialect);

                setProgress(new Progress(0, characters.size()));

                int i = 1;

                for (DocumentModel character : characters) {

                  String[] confusables = ArrayUtils
                      .addAll((String[]) character.getPropertyValue(LC_CONFUSABLES),
                          (String[]) character.getPropertyValue(UC_CONFUSABLES));

                  if (ArrayUtils.isNotEmpty(confusables)) {
                    for (String confusableChar : confusables) {
                      processWordsForConfusable(session, confusableChar);
                    }
                  }

                  // Create transaction for next batch
                  TransactionHelper.commitOrRollbackTransaction();
                  TransactionHelper.startTransaction();

                  setProgress(new Progress(i, characters.size()));
                  ++i;
                }

                setStatus("Done");
                maintenanceLogger.removeFromRequiredJobs(dialect, job, true);
              } catch (Exception e) {
                setStatus("Failed");
                maintenanceLogger.removeFromRequiredJobs(dialect, job, false);
                workFailed(new NuxeoException(
                    "worker" + job + " failed on " + dialect.getTitle() + ": " + e.getMessage()));
              }
            });
  }

  /**
   * Method will find all the dictionary items that contain a confusable character Clean those
   * confusables (i.e. convert to the correct character), then publish, If no changes exist on the
   * document
   *
   * @param session
   * @param confusableChar
   */
  private void processWordsForConfusable(CoreSession session, String confusableChar) {

    for (DocumentModel dictionaryItem : cleanupCharactersService
        .getAllWordsPhrasesForConfusable(session, confusableChar, batchSize)) {

      // Check for unpublished changes (before we clean)
      FirstVoicesPublisherService firstVoicesPublisherService = Framework
          .getService(FirstVoicesPublisherService.class);

      UnpublishedChangesService unpublishedChangesService = Framework
          .getService(UnpublishedChangesService.class);

      boolean unpublishedChangesExist = unpublishedChangesService
          .checkUnpublishedChanges(session, dictionaryItem);

      // Clean confusables for document
      cleanupCharactersService.cleanConfusables(session, dictionaryItem, true);

      if (!unpublishedChangesExist && dictionaryItem.getCurrentLifeCycleState()
          .equals(PUBLISHED_STATE)) {
        firstVoicesPublisherService.republish(dictionaryItem);
      }
    }
  }

  @Override
  public String getTitle() {
    return Constants.CLEAN_CONFUSABLES_JOB_ID;
  }

  @Override
  public String getCategory() {
    return ca.firstvoices.maintenance.Constants.EXECUTE_REQUIRED_JOBS_EVENT_ID;
  }
}
