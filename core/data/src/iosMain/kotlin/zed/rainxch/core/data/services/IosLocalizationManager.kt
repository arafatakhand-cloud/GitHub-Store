package zed.rainxch.core.data.services

import platform.Foundation.NSLocale
import platform.Foundation.NSLocaleCountryCode
import platform.Foundation.NSLocaleLanguageCode
import platform.Foundation.currentLocale

class IosLocalizationManager : LocalizationManager {
    override fun getCurrentLanguageCode(): String {
        val language = getPrimaryLanguageCode()
        val country = NSLocale.currentLocale.objectForKey(NSLocaleCountryCode) as? String
        return if (!country.isNullOrEmpty()) "$language-$country" else language
    }

    override fun getPrimaryLanguageCode(): String =
        NSLocale.currentLocale.objectForKey(NSLocaleLanguageCode) as? String ?: "en"
}
