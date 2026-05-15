package com.vero.repolens.ui.navigation

object NavRoutes {
    const val OVERVIEW = "overview"
    const val SEARCH = "search"
    const val ARCHITECTURE = "architecture"
    const val MODULES = "modules"
    const val MODULE_DETAIL = "module_detail/{moduleId}"
    const val FEATURES = "features"
    const val FEATURE_DETAIL = "feature_detail/{featureId}"
    const val DEPENDENCY_INJECTION = "dependency_injection"
    const val CONCURRENCY = "concurrency"
    const val UI_LAYER = "ui_layer"
    const val TESTING = "testing"
    const val RISKS = "risks"
    const val PR_READINESS = "pr_readiness"
    const val ACTION_ITEMS = "action_items"
    const val PERFORMANCE = "performance"
    const val RECOMMENDATIONS = "recommendations"
    
    fun moduleDetail(moduleId: String) = "module_detail/$moduleId"
    fun featureDetail(featureId: String) = "feature_detail/$featureId"
}

// Made with Bob
