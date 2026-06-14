package app.chiggi.foxone.patches.update

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * com.app.core.domain.entity.appupdate.AppUpdateConfig#getEnabled()
 *
 * The remote app-update / kill-switch config. `enabled` gates whether the (force/optional) update
 * prompt is shown. Forcing it to false disables the forced-update / kill-switch screen.
 */
internal object AppUpdateConfigEnabledFingerprint : Fingerprint(
    name = "getEnabled",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Z",
    parameters = listOf(),
    definingClass = "Lcom/app/core/domain/entity/appupdate/AppUpdateConfig;",
)
