package com.aptoide.uploader.apps.view;

import android.util.Log;
import com.aptoide.uploader.apps.InstalledApp;
import com.aptoide.uploader.apps.UploadDraft;
import com.aptoide.uploader.apps.UploadManager;
import com.aptoide.uploader.view.Presenter;
import com.aptoide.uploader.view.View;
import io.reactivex.Completable;
import io.reactivex.Observable;
import java.util.concurrent.TimeUnit;

public class NotificationPresenter implements Presenter {
  private final NotificationView view;
  private final UploadManager uploadManager;

  public NotificationPresenter(NotificationView view, UploadManager uploadManager) {
    this.view = view;
    this.uploadManager = uploadManager;
  }

  @Override public void present() {
    checkUploads();
  }

  private void checkUploads() {
    view.getLifecycleEvent()
        .filter(event -> event.equals(View.LifecycleEvent.CREATE))
        .flatMap(__ -> uploadManager.getDrafts())
        .flatMapIterable(drafts -> drafts)
        .filter(uploadDraft -> !uploadDraft.getStatus()
            .equals(UploadDraft.Status.IN_QUEUE))
        .flatMap(draft -> {
          if (draft.getStatus()
              .equals(UploadDraft.Status.PROGRESS)) {
            return updateProgress(draft);
          } else {
            return Observable.just(draft);
          }
        })
        .concatMap(i -> Observable.just(i)
            .delay(25, TimeUnit.MILLISECONDS))
        .doOnNext(d -> Log.d("notification", "going to show notification " + d.toString()))
        .doOnNext(d -> Log.d("notification", "going to show notification 2" + d.toString()))
        .flatMapCompletable(draft -> showNotification(draft.getInstalledApp(), draft.getMd5(),
            draft.getStatus()).delay(100, TimeUnit.MILLISECONDS)
            .toObservable()
            .filter(o -> draft.getStatus()
                .equals(UploadDraft.Status.COMPLETED))
            .flatMapCompletable(
                __ -> showNotification(draft.getInstalledApp(), draft.getMd5(), draft.getStatus())))
        .subscribe();
  }

  private Completable showNotification(InstalledApp installedApp, String md5,
      UploadDraft.Status status) {
    Log.d("notification", "going to show notification 3"
        + status
        + " installedapp= "
        + installedApp.getPackageName());
    String appName = installedApp.getName();
    String packageName = installedApp.getPackageName();

    switch (status) {
      case METADATA_SET:
      case DRAFT_CREATED:
      case MD5S_SET:
      case STATUS_SET_DRAFT:
      case STATUS_SET_PENDING:
        view.showPendingUploadNotification(appName, packageName);
        break;
      case WAITING_UPLOAD_CONFIRMATION:
      case UPLOAD_PENDING:
      case META_DATA_ADDED:
      case SET_STATUS_TO_DRAFT:
      case NOT_EXISTENT:
      case PROGRESS:
      case IN_QUEUE:
        break;
      case NO_META_DATA:
        view.showNoMetaDataNotification(appName, packageName, md5);
        break;
      case COMPLETED:
        view.showCompletedUploadNotification(appName, packageName);
        return uploadManager.removeUploadFromPersistence(md5);
      case DUPLICATE:
        view.showDuplicateUploadNotification(appName, packageName);
        return uploadManager.removeUploadFromPersistence(md5);
      case EXCEEDED_GET_RETRIES:
        view.showGetRetriesExceededNotification(appName, packageName);
        return uploadManager.removeUploadFromPersistence(md5);
      case INFECTED:
        view.showUploadInfectionNotificaton(appName, packageName);
        return uploadManager.removeUploadFromPersistence(md5);
      case PUBLISHER_ONLY:
        view.showPublisherOnlyNotification(appName, packageName);
        return uploadManager.removeUploadFromPersistence(md5);
      case INVALID_SIGNATURE:
        view.showInvalidSignatureNotification(appName, packageName);
        return uploadManager.removeUploadFromPersistence(md5);
      case INTELLECTUAL_RIGHTS:
        view.showIntellectualRightsNotification(appName, packageName);
        return uploadManager.removeUploadFromPersistence(md5);
      case CATAPPULT_CERTIFIED:
        view.showCatappultCertifiedNotification(appName, packageName);
        return uploadManager.removeUploadFromPersistence(md5);
      case APP_BUNDLE:
        view.showAppBundleNotification(appName, packageName);
        return uploadManager.removeUploadFromPersistence(md5);
      case ANTI_SPAM_RULE:
        view.showAntiSpamRuleNotification(appName, packageName);
        return uploadManager.removeUploadFromPersistence(md5);
      case UPLOAD_FAILED_RETRY:
        view.showFailedUploadWithRetryNotification(appName, packageName);
        return uploadManager.removeUploadFromPersistence(md5);
      case UPLOAD_FAILED:
        view.showFailedUploadNotification(appName, packageName);
        return uploadManager.removeUploadFromPersistence(md5);
      case UNKNOWN_ERROR_RETRY:
        view.showUnknownErrorRetryNotification(appName, packageName);
        return uploadManager.removeUploadFromPersistence(md5);
      case CLIENT_ERROR:
      case UNKNOWN_ERROR:
      default:
        view.showUnknownErrorNotification(appName, packageName);
        return uploadManager.removeUploadFromPersistence(md5);
    }
    return Completable.complete();
  }

  private Observable<UploadDraft> updateProgress(UploadDraft draft) {
    return uploadManager.getProgress(draft.getInstalledApp()
        .getPackageName())
        .sample(500, TimeUnit.MILLISECONDS)
        .doOnNext(uploadProgress -> view.updateUploadProgress(draft.getInstalledApp()
            .getName(), draft.getInstalledApp()
            .getPackageName(), uploadProgress.getProgress()))
        .map(__ -> draft)
        .doOnError(__ -> view.showUnknownErrorRetryNotification(draft.getInstalledApp()
            .getName(), draft.getInstalledApp()
            .getPackageName()));
  }
}

