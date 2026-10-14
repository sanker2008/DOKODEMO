package com.dokodemo.data.preferences

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class CustomRoutingRuleGsonTest {
    private val gson = Gson()
    private val ruleListType = object : TypeToken<List<CustomRoutingRule>>() {}.type

    @Test fun persistedRuleJsonRoundTripsWithStableNames() {
        val saved = """[{"id":"rule-1","name":"Work","matchType":"DOMAIN_SUFFIX","value":"example.com","action":"DIRECT","enabled":false}]"""
        val expected = listOf(
            CustomRoutingRule(
                id = "rule-1",
                name = "Work",
                matchType = CustomRuleMatchType.DOMAIN_SUFFIX,
                value = "example.com",
                action = CustomRuleAction.DIRECT,
                enabled = false
            )
        )

        val restored: List<CustomRoutingRule> = gson.fromJson(saved, ruleListType)
        assertEquals(expected, restored)

        val serialized = JsonParser.parseString(gson.toJson(restored)).asJsonArray[0].asJsonObject
        assertEquals("rule-1", serialized.get("id").asString)
        assertEquals("Work", serialized.get("name").asString)
        assertEquals("DOMAIN_SUFFIX", serialized.get("matchType").asString)
        assertEquals("example.com", serialized.get("value").asString)
        assertEquals("DIRECT", serialized.get("action").asString)
        assertFalse(serialized.get("enabled").asBoolean)
    }
}
