/*
 * Copyright 2015 Bill Davidson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kopitubruk.util.json;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TimeZone;
import java.util.Vector;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import jdk.nashorn.api.scripting.ScriptObjectMirror;

//import java.text.SimpleDateFormat;
//import org.junit.Rule;
//import org.junit.rules.TestRule;
//import org.junit.rules.TestWatcher;
//import org.junit.runner.Description;

/**
 * Tests for JSONUtil. Most of the produced JSON is put through Java's script
 * engine so that it will be tested that it parses without error. In most cases,
 * the JSON is tested both against Javascript eval() and JSON.parse(). In
 * general, eval() is looser than JSON.parse() except for property names where
 * JSON.parse() is looser because eval() requires ECMAScript compliant property
 * names while JSON.parse() only requires compliance with the JSON standard
 * which allows almost all defined Unicode code points.
 *
 * @author Bill Davidson
 */
public class TestJSONUtil
{
    private static final Log s_log = LogFactory.getLog(TestJSONUtil.class);

    //private static SimpleDateFormat s_sdf;

    /**
     * Print out the name of the currently running test.
     *
    @Rule
    public TestRule watcher = new TestWatcher()
    {
        protected void starting( Description description )
        {
            System.out.println(s_sdf.format(new Date()) + ' ' + description.getMethodName());
        }
    }; */

    /**
     * Create a dummy JNDI initial context in order to avoid having
     * JNDI code throw an exception during the JUnit tests.
     */
    @BeforeClass
    public static void setUpClass()
    {
        try{
            Context ctx = JNDIUtil.createEnvContext(JSONUtil.class.getPackage().getName().replaceAll("\\.", "/"));

            ctx.bind("appName", "TestJSONUtil");
            ctx.bind("reflectClass0", "org.kopitubruk.util.json.ReflectTestClass,a,e,e=k");
            ctx.bind("preciseNumbers", true);
        }catch ( NamingException e ){
            s_log.fatal("Couldn't create context", e);
            System.exit(-1);
        }

        String validateJs = "validate.js";
        try{
            // Get the Javascript engine and load the validation javascript file into it.
            ScriptEngine engine = new ScriptEngineManager().getEngineByExtension("js");
            String validateJsFile = TestJSONUtil.class.getResource(validateJs).getFile();
            engine.eval(new BufferedReader(new FileReader(validateJsFile)));
            invocable = (Invocable)engine;
        }catch ( ScriptException|FileNotFoundException e ){
            // Can't validate any JSON.
            s_log.fatal("Couldn't load " + validateJs, e);
            System.exit(-1);
        }

        /*
         * Some of these tests depend upon error messages which need to be in
         * English, so it's forced during the tests.
         */
        JSONConfigDefaults.setLocale(Locale.US);
    }

    /**
     * Cleanup resources.
     */
    @AfterClass
    public static void cleanUpResources()
    {
        JSONConfigDefaults.clearMBean();
    }

    /**
     * Javascript engine to be used to validate JSON. Nashorn (Java 8) supports
     * ECMAScript 5.1. Java 7 uses Rhino 1.7, which supports something roughly
     * close to ECMAScript 3.
     */
    private static Invocable invocable;

    /**
     * Make sure that the JSON parses. Using a script and Invocable because that
     * makes the data get sent to the script raw, as it would in an AJAX call.
     * I used to do a direct eval but that caused some issues for some things
     * with regard to escapes.
     *
     * @param json A JSON string.
     * @param func the Javascript function to call.
     * @return the object returned by the function or null if there's an exception thrown.
     * @throws ScriptException if the JSON doesn't evaluate properly.
     * @throws NoSuchMethodException If it can't find the Javascript function to use for validation.
     */
    private Object runValidateJSON( String json, String func ) throws ScriptException, NoSuchMethodException
    {
        // make sure it parses.
        Object result = null;
        try{
            // this sends the raw JSON data to the function as an argument.
            result = invocable.invokeFunction(func, json);
        }catch ( ScriptException e ){
            boolean lastCode = false;
            StringBuilder buf = new StringBuilder(func).append("()\n");
            for ( int i = 0, j = 0, len = json.length(); i < len && j < 500; j++ ){
                int codePoint = json.codePointAt(i);
                int charCount = Character.charCount(codePoint);
                if ( codePoint < 256 && codePoint >= ' ' ){
                    if ( lastCode ){
                        buf.append('\n');
                    }
                    buf.appendCodePoint(codePoint);
                    lastCode = false;
                }else{
                    buf.append(String.format("\n%d U+%04X %s %d",
                                             i, codePoint,
                                             Character.getName(codePoint),
                                             Character.getType(codePoint)));
                    lastCode = true;
                }
                i += charCount;
            }
            s_log.error(buf.toString(), e);
            throw e;
        }catch ( NoSuchMethodException e ){
            s_log.error("Couldn't invoke "+func+"()", e);
            throw e;
        }
        return result;
    }

    /**
     * Validate the given JSON string with Javascript eval(String).
     *
     * @param json A JSON string.
     * @return the object returned by the function or null if there's an exception thrown.
     * @throws ScriptException if the JSON doesn't evaluate properly.
     * @throws NoSuchMethodException If it can't find the Javascript function to use for validation.
     */
    private Object evalJSON( String json ) throws ScriptException, NoSuchMethodException
    {
        return runValidateJSON(json, "evalJSON");
    }

    /**
     * Validate the given JSON string with Javascript JSON.parse(String).
     *
     * @param json A JSON string.
     * @return the object returned by the function or null if there's an exception thrown.
     * @throws ScriptException if the JSON doesn't evaluate properly.
     * @throws NoSuchMethodException If it can't find the Javascript function to use for validation.
     */
    private Object parseJSON( String json ) throws ScriptException, NoSuchMethodException
    {
        return runValidateJSON(json, "parseJSON");
    }

    /**
     * Validate the given JSON string with both Javascript eval(String) and JSON.parse(String).
     *
     * @param json A JSON string.
     * @throws ScriptException if the JSON doesn't evaluate properly.
     * @throws NoSuchMethodException If it can't find the Javascript function to use for validation.
     */
    private void validateJSON( String json ) throws ScriptException, NoSuchMethodException
    {
        evalJSON(json);
        parseJSON(json);
    }

    /**
     * Test all characters allowed for property names. Every start character
     * gets tested in the start position and most part characters get tested
     * twice but all at least once.
     * <p>
     * This test is slow because it's doing over 100,000 validations through
     * the Javascript interpreter, which is slow.  If you comment out that
     * validation, then this test takes about 1 second on my laptop, but you
     * only test that none of the valid code points cause an exception and
     * not that they won't cause a syntax error.  With the validation on it
     * typically takes about 68 seconds for this to run on my laptop, which
     * is annoying but it provides a better test.
     * <p>
     * It should be noted that some valid characters need the identifier to
     * be in quotes in order for the validation to work properly so the
     * validation tests that those identifiers that need that do get quoted
     * even though I turned identifier quoting off for the tests.
     *
     * @throws ScriptException if the JSON doesn't evaluate properly.
     * @throws NoSuchMethodException If it can't find the Javascript function to use for validation.
     */
    @Test
    public void testValidPropertyNames() throws ScriptException, NoSuchMethodException
    {
        JSONConfig cfg = new JSONConfig();

        ArrayList<Integer> validStart = new ArrayList<>();
        ArrayList<Integer> validPart = new ArrayList<>();

        for ( int i = 0; i <= Character.MAX_CODE_POINT; i++ ){
            if ( JSONUtil.isValidIdentifierStart(i, cfg) ){
                validStart.add(i);
            }else if ( JSONUtil.isValidIdentifierPart(i, cfg) ){
                validPart.add(i);
            }
        }
        validStart.trimToSize();
        s_log.debug(validStart.size() + " valid start code points");
        validPart.addAll(validStart);
        Collections.sort(validPart);
        s_log.debug(validPart.size() + " valid part code points");

        final int MAX_LENGTH = 3;
        int[] propertyName = new int[MAX_LENGTH];
        int startIndex = 0;
        int partIndex = 0;
        int nameIndex = 0;

        JsonObject jsonObj = new JsonObject(1, cfg);

        int startSize = validStart.size();
        int partSize = validPart.size();
        propertyName[nameIndex++] = validStart.get(startIndex++);

        while ( startIndex < startSize ){
            propertyName[nameIndex++] = validPart.get(partIndex++);
            if ( nameIndex == MAX_LENGTH ){
                jsonObj.clear();
                jsonObj.add(new String(propertyName,0,nameIndex), 0);
                String json = jsonObj.toJSON();
                validateJSON(json);    // this makes this test take a long time to run.
                nameIndex = 0;
                if ( startIndex < startSize ){
                    // start new string.
                    propertyName[nameIndex++] = validStart.get(startIndex++);
                }
            }
            if ( partIndex == partSize ){
                partIndex = 0;
            }
        }
    }

    /**
     * Test all characters allowed for property names by JSON.parse() but not by
     * Javascript eval(). The JSON standard is much looser with characters
     * allowed in property names than ECMAScript. It allows pretty much any
     * defined code point greater than or equal to 32. There are no start
     * character rules either as there are with ECMAScript identifiers.
     *
     * @throws ScriptException if the JSON doesn't evaluate properly.
     * @throws NoSuchMethodException If it can't find the Javascript function to use for validation.
     */
    @Test
    public void testJSONPropertyNames() throws ScriptException, NoSuchMethodException
    {
        JSONConfig jcfg = new JSONConfig().setFullJSONIdentifierCodePoints(false);
        JSONConfig cfg = new JSONConfig().setFullJSONIdentifierCodePoints(true);

        JsonObject jsonObj = new JsonObject(1, cfg);
        int[] normalIdent = new int[1];
        int[] escName = new int[2];
        escName[0] = '\\';
        int jsonOnlyCount = 0;

        for ( int i = ' '; i <= Character.MAX_CODE_POINT; i++ ){
            if ( JSONUtil.isValidIdentifierStart(i, cfg) && ! JSONUtil.isValidIdentifierPart(i, jcfg) ){
                normalIdent[0] = i;
                jsonObj.clear();
                jsonObj.add(new String(normalIdent,0,1), 0);
                String json = jsonObj.toJSON();
                // these would fail eval().
                parseJSON(json);
                ++jsonOnlyCount;
            }
        }

        s_log.debug(jsonOnlyCount+" code points are valid identifier start characters for JSON.parse() but not for eval()");
    }

    /**
     * Test all characters not allowed for property names by JSON.parse().
     */
    @Test
    public void testBadJSONPropertyNames()
    {
        JSONConfig cfg = new JSONConfig().setFullJSONIdentifierCodePoints(true);

        JsonObject jsonObj = new JsonObject(1, cfg);
        int[] codePoints = new int[256];
        int j = 0;

        for ( int i = 0; i <= Character.MAX_CODE_POINT; i++ ){
            if (  i < ' ' || ! Character.isDefined(i) ){
                codePoints[j++] = i;
                if ( j == codePoints.length ){
                    testBadIdentifier(codePoints, 0, j, jsonObj);
                    j = 0;
                }
            }
        }
        if ( j > 0 ){
            testBadIdentifier(codePoints, 0, j, jsonObj);
        }
    }

    /**
     * Test bad code points for the start of characters in names.
     */
    @Test
    public void testBadStartPropertyNames()
    {
        JSONConfig cfg = new JSONConfig();
        JsonObject jsonObj = new JsonObject(1, cfg);
        int[] codePoints = new int[1];

        for ( int i = 0; i <= Character.MAX_CODE_POINT; i++ ){
            if ( ! JSONUtil.isValidIdentifierStart(i, cfg) ){
                codePoints[0] = i;
                testBadIdentifier(codePoints, 0, 1, jsonObj);
            }
        }
    }

    /**
     * Test bad code points for non-start characters in names.
     */
    @Test
    public void testBadPartPropertyNames()
    {
        int[] codePoints = new int[256];
        int j = 1;
        codePoints[0] = '_';
        JSONConfig cfg = new JSONConfig();
        JsonObject jsonObj = new JsonObject(1, cfg);

        for ( int i = 0; i <= Character.MAX_CODE_POINT; i++ ){
            if ( isNormalCodePoint(i) && ! JSONUtil.isValidIdentifierStart(i, cfg) && ! JSONUtil.isValidIdentifierPart(i, cfg) ){
                // high surrogates break the test unless they are followed immediately by low surrogates.
                // just skip them.  anyone who sends bad surrogate pairs deserves what they get.
                codePoints[j++] = i;
                if ( j == codePoints.length ){
                    testBadIdentifier(codePoints, 1, j, jsonObj);
                    j = 1;
                }
            }
        }
        if ( j > 1 ){
            testBadIdentifier(codePoints, 1, j, jsonObj);
        }
    }

    /**
     * Test a bad identifier.  This is a utility method used by other test methods.
     *
     * @param codePoints A set of code points with bad code points.
     * @param start The index of the first code point to check for.
     * @param end The index just after the last code point to check for.
     * @param jsonObj A jsonObj to use for the test.
     */
    private void testBadIdentifier( int[] codePoints, int start, int end, JsonObject jsonObj )
    {
        // clear in order to avoid memory abuse.
        // didn't create the object here because it would have to be recreated millions of times.
        jsonObj.clear();
        try{
            jsonObj.add(new String(codePoints,0,end), 0);
            jsonObj.toJSON();
            fail(String.format("Expected a BadPropertyNameException to be thrown for U+%04X", codePoints[start]));
        }catch ( BadPropertyNameException e ){
            String message = e.getMessage();
            for ( int i = start; i < end; i++ ){
                assertThat(message, containsString(String.format("Code point U+%04X", codePoints[i])));
            }
        }
    }

    /**
     * Test valid Unicode strings.
     *
     * @throws ScriptException if the JSON doesn't evaluate properly.
     * @throws NoSuchMethodException If it can't find the Javascript function to use for validation.
     */
    @Test
    public void testValidStrings() throws ScriptException, NoSuchMethodException
    {
        int[] codePoints = new int[32768];
        JSONConfig cfg = new JSONConfig();
        JsonObject jsonObj = new JsonObject(1, cfg);
        int j = 0;

        for ( int i = 0; i <= Character.MAX_CODE_POINT; i++ ){
            if ( isNormalCodePoint(i) ){
                // 247650 code points, the last time I checked.
                codePoints[j++] = i;
                if ( j == codePoints.length ){
                    jsonObj.clear();
                    jsonObj.add("x", new String(codePoints,0,j));
                    String json = jsonObj.toJSON();
                    validateJSON(json);
                    j = 0;
                }
            }
        }
        if ( j > 0 ){
            jsonObj.clear();
            jsonObj.add("x", new String(codePoints,0,j));
            String json = jsonObj.toJSON();
            validateJSON(json);
        }

        jsonObj.clear();
        jsonObj.add("x", "Some data\nSome other data.");
        String json = jsonObj.toJSON();
        validateJSON(json);
        assertThat(json, is("{\"x\":\"Some data\\nSome other data.\"}"));
    }

    /**
     * Test that Unicode escape sequences in identifiers work.
     *
     * @throws ScriptException if the JSON doesn't evaluate properly.
     * @throws NoSuchMethodException If it can't find the Javascript function to use for validation.
     */
    @Test
    public void testUnicodeEscapeInIdentifier() throws ScriptException, NoSuchMethodException
    {
        JsonObject jsonObj = new JsonObject(1);
        String[] ids = { "a\\u1234", "\\u1234x" };
        for ( String id : ids ){
            jsonObj.clear();
            jsonObj.add(id, 0);

            String json = jsonObj.toJSON();
            validateJSON(json);
            assertThat(json, is("{\""+id+"\":0}"));
        }
    }

    /**
     * Test ECMAScript 6 code point escapes.
     */
    @Test
    public void testECMA6UnicodeEscapeInString()
    {
        JSONConfig cfg = new JSONConfig().setUseECMA6(true).setEscapeNonAscii(true)
                                         .setBadCharacterPolicy(JSONConfig.ESCAPE);
        StringBuilder buf = new StringBuilder();
        Set<Character> singles = new HashSet<>(Arrays.asList('\b','\t','\n','\f','\r'));
        Random rand = new Random();
        int bound = Character.MAX_CODE_POINT+1;
        int min = Character.MIN_SUPPLEMENTARY_CODE_POINT;
        for ( int i = 0; i < 4096; i++ ){
            int cp;
            do{
                cp = rand.nextInt(bound);
            }while ( cp > 0xF && cp < min );
            buf.setLength(0);
            buf.appendCodePoint(cp);
            String result;
            if ( cp < 0xF && singles.contains((char)cp) ){
                result = '"' + StringProcessor.getEscape((char)cp) + '"';
            }else{
                result = '"' + String.format("\\u{%X}", cp) + '"';
            }
            String json = JSONUtil.toJSON(buf, cfg);
            assertThat(json, is(result));
        }
    }

    /**
     * Test code unit escapes.
     */
    @Test
    public void testEscapeGenerator()
    {
        JSONConfig cfg = new JSONConfig().setUseECMA6(false).setEscapeNonAscii(true)
                                         .setBadCharacterPolicy(JSONConfig.ESCAPE);
        StringBuilder buf = new StringBuilder();
        Set<Character> singles = new HashSet<>(Arrays.asList('\b','\t','\n','\f','\r'));
        Random rand = new Random();
        int bound = Character.MAX_CODE_POINT+1;
        int min = Character.MIN_SUPPLEMENTARY_CODE_POINT;
        for ( int i = 0; i < 4096; i++ ){
            int cp;
            do{
                cp = rand.nextInt(bound);
            }while ( cp >= ' ' && cp < 128 );
            buf.setLength(0);
            buf.appendCodePoint(cp);
            String result;
            if ( cp < min ){
                if ( cp < 0xF && singles.contains((char)cp) ){
                    result = '"' + StringProcessor.getEscape((char)cp) + '"';
                }else{
                    result = '"' + String.format("\\u%04X", cp) + '"';
                }
            }else{
                int high = Character.highSurrogate(cp);
                int low = Character.lowSurrogate(cp);
                result = '"' + String.format("\\u%04X\\u%04X", high, low) + '"';
            }
            String json = JSONUtil.toJSON(buf, cfg);
            assertThat(json, is(result));
        }
    }

    private static final int BAD_CHARS = 4096;


    /**
     * Utility for other test methods.
     *
     * @param val the string version of the character being tested.
     * @return the result.
     */
    private String makeResult( CharSequence val )
    {
        return "{\"a\":\"" + val +                  // start
               "a\",\"b\":\"a" + val +              // end
               "\",\"c\":\"" + val +                // alone
               "\",\"d\":\"a" + val + "a\"}";       // embedded
    }

    /**
     * Utility for other test methods.
     *
     * @param ch the character being tested.
     * @return the result.
     */
    private String makeResult( char ch )
    {
        return makeResult(Character.valueOf(ch).toString());
    }

    /**
     * Make the surrogate test map and return it.
     *
     * @param ch the character being tested.
     * @return the map.
     */
    private JsonObject surrogateTestMap( char ch )
    {
        JsonObject jsonObj = new JsonObject(4);

        jsonObj.add("a", ch+"a");               // start
        jsonObj.add("b", "a"+ch);               // end
        jsonObj.add("c", ch);                   // alone
        jsonObj.add("d", "a" + ch + "a");       // embedded

        return jsonObj;
    }

    /**
     * Utility for other tests.
     *
     * @param rand a random number generator.
     * @return a random surrogate.
     */
    private char getRandomSurrogate( Random rand )
    {
        final int bound = 1 + Character.MAX_SURROGATE - Character.MIN_SURROGATE;

        char ch;
        do{
            ch = (char)(rand.nextInt(bound) + Character.MIN_SURROGATE);
        }while ( ! Character.isSurrogate(ch) || ! Character.isDefined(ch) );

        return ch;
    }

    /**
     * Test bad character replacement.
     */
    @Test
    public void testReplaceSurrogates()
    {
        JSONConfig cfg = new JSONConfig().setUseECMA6(false).setBadCharacterPolicy(JSONConfig.REPLACE);
        Random rand = new Random();
        final String result = makeResult(StringProcessor.UNICODE_REPLACEMENT_CHARACTER);

        for ( int i = 0; i <= BAD_CHARS; i++ ){
            assertThat(JSONUtil.toJSON(surrogateTestMap(getRandomSurrogate(rand)), cfg), is(result));
        }
    }

    /**
     * Test bad character discard.
     */
    @Test
    public void testDiscardSurrogates()
    {
        JSONConfig cfg = new JSONConfig().setUseECMA6(false).setBadCharacterPolicy(JSONConfig.DISCARD);
        Random rand = new Random();
        final String result = makeResult("");

        for ( int i = 0; i <= BAD_CHARS; i++ ){
            assertThat(JSONUtil.toJSON(surrogateTestMap(getRandomSurrogate(rand)), cfg), is(result));
        }
    }

    /**
     * Test bad character exception.
     */
    @Test
    public void testExceptionSurrogates()
    {
        JSONConfig cfg = new JSONConfig().setUseECMA6(false).setBadCharacterPolicy(JSONConfig.EXCEPTION);
        Random rand = new Random();

        for ( int i = 0; i <= BAD_CHARS; i++ ){
            char ch = getRandomSurrogate(rand);
            try{
                JSONUtil.toJSON(surrogateTestMap(ch), cfg);
                fail("Expected a UnmatchedSurrogateException to be thrown.");
            }catch ( UnmatchedSurrogateException e ){
                assertThat(e.getMessage(), containsString(String.format("Unmatched surrogate U+%04X at position", (int)ch)));
            }
        }
    }

    /**
     * Test bad character escape.
     */
    @Test
    public void testEscapeBadSurrogates()
    {
        JSONConfig cfg = new JSONConfig().setUseECMA6(false).setBadCharacterPolicy(JSONConfig.ESCAPE);
        Random rand = new Random();

        for ( int i = 0; i <= BAD_CHARS; i++ ){
            char ch = getRandomSurrogate(rand);
            assertThat(JSONUtil.toJSON(surrogateTestMap(ch), cfg), is(makeResult(String.format("\\u%04X", (int)ch))));
        }
    }

    /**
     * Test bad character pass.
     */
    @Test
    public void testPassSurrogates()
    {
        JSONConfig cfg = new JSONConfig().setUseECMA6(false).setBadCharacterPolicy(JSONConfig.PASS);
        Random rand = new Random();

        for ( int i = 0; i <= BAD_CHARS; i++ ){
            char ch = getRandomSurrogate(rand);
            assertThat(JSONUtil.toJSON(surrogateTestMap(ch), cfg), is(makeResult(ch)));
        }
    }

    /**
     * Make the undefined test map and return it.
     *
     * @param cp the code point being tested.
     * @return the map.
     */
    private JsonObject undefinedTestMap( int cp )
    {
        JsonObject jsonObj = new JsonObject(4);

        StringBuilder buf = new StringBuilder();

        buf.setLength(0);
        buf.appendCodePoint(cp);            // start
        buf.append("a");
        jsonObj.add("a", buf.toString());

        buf.setLength(0);
        buf.append("a");
        buf.appendCodePoint(cp);            // end
        jsonObj.add("b", buf.toString());

        buf.setLength(0);
        buf.appendCodePoint(cp);            // alone
        jsonObj.add("c", buf.toString());

        buf.setLength(0);
        buf.append("a");
        buf.appendCodePoint(cp);            // embedded
        buf.append("a");
        jsonObj.add("d", buf.toString());

        return jsonObj;
    }

    /**
     * Utility for other tests.
     *
     * @param rand a random number generator.
     * @return a random undefined code point.
     */
    private int getRandomUndefined( Random rand )
    {
        final int bound = Character.MAX_CODE_POINT + 1;

        int cp;
        do{
            cp = rand.nextInt(bound);
        }while ( Character.isDefined(cp) || (cp < Character.MIN_SUPPLEMENTARY_CODE_POINT && Character.isSurrogate((char)cp)) );

        return cp;
    }

    /**
     * Test bad character replacement.
     */
    @Test
    public void testReplaceUndefined()
    {
        JSONConfig cfg = new JSONConfig().setUseECMA6(false).setBadCharacterPolicy(JSONConfig.REPLACE);
        Random rand = new Random();
        final String result = makeResult(StringProcessor.UNICODE_REPLACEMENT_CHARACTER);

        for ( int i = 0; i <= BAD_CHARS; i++ ){
            assertThat(JSONUtil.toJSON(undefinedTestMap(getRandomUndefined(rand)), cfg), is(result));
        }
    }

    /**
     * Test bad character discard.
     */
    @Test
    public void testDiscardUndefined()
    {
        JSONConfig cfg = new JSONConfig().setUseECMA6(false).setBadCharacterPolicy(JSONConfig.DISCARD);
        Random rand = new Random();
        final String result = makeResult("");

        for ( int i = 0; i <= BAD_CHARS; i++ ){
            assertThat(JSONUtil.toJSON(undefinedTestMap(getRandomUndefined(rand)), cfg), is(result));
        }
    }

    /**
     * Test bad character exception.
     */
    @Test
    public void testExceptionUndefined()
    {
        JSONConfig cfg = new JSONConfig().setUseECMA6(false).setBadCharacterPolicy(JSONConfig.EXCEPTION);
        Random rand = new Random();

        for ( int i = 0; i <= BAD_CHARS; i++ ){
            int cp = getRandomUndefined(rand);
            try{
                JSONUtil.toJSON(undefinedTestMap(cp), cfg);
                fail("Expected a UndefinedCodePointException to be thrown.");
            }catch ( UndefinedCodePointException e ){
                assertThat(e.getMessage(), containsString(String.format("Undefined code point U+%04X at position", cp)));
            }
        }
    }

    /**
     * Test bad character escape.
     */
    @Test
    public void testEscapeUndefined()
    {
        JSONConfig cfg = new JSONConfig().setUseECMA6(false).setBadCharacterPolicy(JSONConfig.ESCAPE);
        Random rand = new Random();

        for ( int i = 0; i <= BAD_CHARS; i++ ){
            int cp = getRandomUndefined(rand);
            String escape;
            if ( cp < Character.MIN_SUPPLEMENTARY_CODE_POINT ){
                escape = String.format("\\u%04X", cp);
            }else{
                escape = String.format("\\u%04X\\u%04X", (int)Character.highSurrogate(cp), (int)Character.lowSurrogate(cp));
            }
            assertThat(JSONUtil.toJSON(undefinedTestMap(cp), cfg), is(makeResult(escape)));
        }
    }

    /**
     * Test bad character pass.
     */
    @Test
    public void testPassUndefined()
    {
        JSONConfig cfg = new JSONConfig().setUseECMA6(false).setBadCharacterPolicy(JSONConfig.PASS);
        StringBuilder buf = new StringBuilder();
        Random rand = new Random();

        for ( int i = 0; i <= BAD_CHARS; i++ ){
            int cp = getRandomUndefined(rand);
            buf.setLength(0);
            buf.appendCodePoint(cp);
            assertThat(JSONUtil.toJSON(undefinedTestMap(cp), cfg), is(makeResult(buf)));
        }
    }

    /**
     * Test that Unicode escape sequences in identifiers work.
     *
     * @throws ScriptException if the JSON doesn't evaluate properly.
     * @throws NoSuchMethodException If it can't find the Javascript function to use for validation.
     */
    @Test
    public void testEscapePassThrough() throws ScriptException, NoSuchMethodException
    {
        Map<String,Object> jsonObj = new HashMap<>();
        JSONConfig cfg = new JSONConfig().setUnEscapeWherePossible(false)
                                         .setPassThroughEscapes(true)
                                         .setUseECMA6(true);
        // escapes that get passed through.
        String[] strs = { "\\u1234", "a\\u{41}", "\\\"", "\\/", "\\b", "\\f", "\\n", "\\r", "\\t", "\\\\", "\\v" };
        for ( String str : strs ){
            jsonObj.clear();
            jsonObj.put("x", str);

            String json = JSONUtil.toJSON(jsonObj, cfg);
            String result = "\\v".equals(str) ? "\\u{B}" : str;
            if ( result.indexOf('{') < 0 ){
                // Nashorn doesn't understand ECMAScript 6 code point escapes.
                validateJSON(json);
            }
            assertThat(json, is("{\"x\":\""+result+"\"}"));
        }

        // test it with ECMA6 disabled.
        String str = "a\\u{41}";
        jsonObj.clear();
        jsonObj.put("x", str);
        cfg.setUseECMA6(false);
        String json = JSONUtil.toJSON(jsonObj, cfg);
        validateJSON(json);
        assertThat(json, is("{\"x\":\"aA\"}"));
    }

    /**
     * Test that unescape works.
     *
     * @throws ScriptException if the JSON doesn't evaluate properly.
     * @throws NoSuchMethodException If it can't find the Javascript function to use for validation.
     */
    @Test
    public void testUnEscape() throws ScriptException, NoSuchMethodException
    {
        Map<String,Object> jsonObj = new LinkedHashMap<>();
        String[] strs = {"a\\u0041", "d\\u{41}", "e\\v", "f\\'"};
        JSONConfig cfg = new JSONConfig().setUnEscapeWherePossible(true);
        for ( String str : strs ){
            jsonObj.clear();
            jsonObj.put("x", str+'Z');

            String json = JSONUtil.toJSON(jsonObj, cfg);
            char firstChar = str.charAt(0);
            String result;
            switch ( firstChar ){
                case 'e':
                    // \v unescaped will be re-escaped as a Unicode code unit.
                    result = firstChar + "\\u000B";
                    break;
                case 'f':
                    result = firstChar + "'";
                    break;
                default:
                    result = firstChar + "A";
                    break;
            }
            assertThat(json, is("{\"x\":\""+result+"Z\"}"));
        }

        // test that these get fixed regardless.
        cfg.setUnEscapeWherePossible(false)
           .setPassThroughEscapes(true);

        // test octal/hex unescape.
        for ( int i = 0; i < 256; i++ ){
            jsonObj.clear();
            jsonObj.put("x", String.format("a\\%oZ", i));
            jsonObj.put("y", String.format("a\\x%02XZ", i));
            String result = StringProcessor.getEscape((char)i);
            if ( result == null ){
                result = i < 0x20 ? String.format("\\u%04X", i) : String.format("%c", (char)i);
            }
            String json = JSONUtil.toJSON(jsonObj, cfg);
            validateJSON(json);
            assertThat(json, is("{\"x\":\"a"+result+"Z\",\"y\":\"a"+result+"Z\"}"));
        }
    }

    /**
     * Test the parser.
     *
     * @throws ParseException for parsing problems.
     */
    @Test
    public void testParser() throws ParseException
    {
        Object obj = JSONParser.parseJSON("{\"foo\":\"b\\\\\\\"ar\",\"a\":5,\"b\":2.37e24,c:Infinity,\"d\":NaN,\"e\":[1,2,3,{\"a\":4}]}");
        String json = JSONUtil.toJSON(obj);
        assertEquals("{\"foo\":\"b\\\\\\\"ar\",\"a\":5,\"b\":2.37E24,\"c\":\"Infinity\",\"d\":\"NaN\",\"e\":[1,2,3,{\"a\":4}]}", json);

        obj = JSONParser.parseJSON("'foo'");
        assertEquals("foo", obj);

        obj = JSONParser.parseJSON("2.37e24");
        assertEquals(2.37e24, obj);

        obj = JSONParser.parseJSON("Infinity");
        assertTrue(Double.isInfinite((Double)obj));

        obj = JSONParser.parseJSON("NaN");
        assertTrue(Double.isNaN((Double)obj));

        obj = JSONParser.parseJSON("false");
        assertEquals(Boolean.FALSE, obj);

        obj = JSONParser.parseJSON("null");
        assertEquals(null, obj);

        JSONConfig cfg = new JSONConfig().setUsePrimitiveArrays(true);

        obj = JSONParser.parseJSON("[1.1,2.2,-3.134598765,4.0]", cfg);
        double[] doubles = (double[])obj;
        assertEquals(new Double(1.1), new Double(doubles[0]));
        assertEquals(new Double(2.2), new Double(doubles[1]));
        assertEquals(new Double(-3.134598765), new Double(doubles[2]));
        assertEquals(new Double(4.0), new Double(doubles[3]));

        obj = JSONParser.parseJSON("[1.1,2.2,-3.134,4.0]", cfg);
        float[] floats = (float[])obj;
        assertEquals(new Float(1.1), new Float(floats[0]));
        assertEquals(new Float(2.2), new Float(floats[1]));
        assertEquals(new Float(-3.134), new Float(floats[2]));
        assertEquals(new Float(4.0), new Float(floats[3]));

        obj = JSONParser.parseJSON("[1,2,-3,4]", cfg);
        byte[] bytes = (byte[])obj;
        assertEquals(new Byte((byte)1), new Byte(bytes[0]));
        assertEquals(new Byte((byte)2), new Byte(bytes[1]));
        assertEquals(new Byte((byte)-3), new Byte(bytes[2]));
        assertEquals(new Byte((byte)4), new Byte(bytes[3]));

        // parse various forms of date strings.
        cfg.setEncodeDatesAsStrings(true);
        DateFormat fmt = cfg.getDateGenFormat();

        Date dt = (Date)JSONParser.parseJSON("new Date(\"2015-09-16T14:08:34.034Z\")", cfg);
        assertEquals("2015-09-16T14:08:34.034Z", fmt.format(dt));

        dt = (Date)JSONParser.parseJSON("\"2015-09-16T14:08:34.034Z\"", cfg);
        assertEquals("2015-09-16T14:08:34.034Z", fmt.format(dt));

        dt = (Date)JSONParser.parseJSON("\"2015-09-16T14:08:34.034+01\"", cfg);
        assertEquals("2015-09-16T14:08:34.034Z", fmt.format(dt));

        dt = (Date)JSONParser.parseJSON("\"2015-09-16T14:08:34.034+01:30\"", cfg);
        assertEquals("2015-09-16T12:38:34.034Z", fmt.format(dt));

        dt = (Date)JSONParser.parseJSON("\"2015-09-16T14:08:34\"", cfg);
        assertEquals("2015-09-16T14:08:34.034Z", fmt.format(dt));

        dt = (Date)JSONParser.parseJSON("\"2015-09-16T14:08:34+01:30\"", cfg);
        assertEquals("2015-09-16T12:38:34.034Z", fmt.format(dt));

        // custom formats.
        DateFormat nfmt = cfg.setDateGenFormat("EEE, d MMM yyyy HH:mm:ss Z");
        nfmt.setTimeZone(TimeZone.getTimeZone("US/Eastern"));
        cfg.addDateParseFormat("yyyy.MM.dd G 'at' HH:mm:ss z");
        dt = (Date)JSONParser.parseJSON("\"2001.07.04 AD at 12:08:56 EDT\"", cfg);
        assertEquals("Wed, 4 Jul 2001 12:08:56 -0400", nfmt.format(dt));

        // test that the old one still works.
        dt = (Date)JSONParser.parseJSON("\"2015-09-16T14:08:34+01:30\"", cfg);
        assertEquals("2015-09-16T12:38:34.034Z", fmt.format(dt));

        try{
            JSONParser.parseJSON("{\"foo\":\"b\\\\\\\"ar\",\"a\":5,\"b\":2.37e24,\"c\":&*^,\"d\":NaN,\"e\":[1,2,3,{\"a\":4}]}");
            fail("Expected JSONParserException for bad data");
        }catch ( JSONParserException e ){
        }
    }

    /**
     * Test using reserved words in identifiers.
     *
     * @throws ScriptException if the JSON doesn't evaluate properly.
     * @throws NoSuchMethodException If it can't find the Javascript function to use for validation.
     */
    @Test
    public void testReservedWordsInIdentifiers() throws ScriptException, NoSuchMethodException
    {
        Map<String,Object> jsonObj = new HashMap<>();
        JSONConfig cfg = new JSONConfig().setAllowReservedWordsInIdentifiers(true);
        Set<String> reservedWords = JSONUtil.getJavascriptReservedWords();
        for ( String reservedWord : reservedWords ){
            jsonObj.clear();
            jsonObj.put(reservedWord, 0);

            // test with allow reserved words.
            String json = JSONUtil.toJSON(jsonObj, cfg);
            validateJSON(json);
            assertThat(json, is("{\""+reservedWord+"\":0}"));

            // test with reserved words disallowed.
            try{
                JSONUtil.toJSON(jsonObj);
                fail("Expected BadPropertyNameException for reserved word "+reservedWord);
            }catch ( BadPropertyNameException e ){
                String message = e.getMessage();
                assertThat(message, is(reservedWord+" is a reserved word."));
            }
        }
    }

    /**
     * Test EscapeBadIdentifierCodePoints
     *
     * @throws ScriptException if the JSON doesn't evaluate properly.
     * @throws NoSuchMethodException If it can't find the Javascript function to use for validation.
     */
    @Test
    public void testEscapeBadIdentifierCodePoints() throws ScriptException, NoSuchMethodException
    {
        Map<Object,Object> jsonObj = new LinkedHashMap<>();
        StringBuilder buf = new StringBuilder("c");
        StringBuilder cmpBuf = new StringBuilder();
        JSONConfig cfg = new JSONConfig().setEscapeBadIdentifierCodePoints(true);
        jsonObj.put("x\u0005", 0);

        String json = JSONUtil.toJSON(jsonObj, cfg);
        validateJSON(json);
        assertThat(json, is("{\"x\\u0005\":0}"));

        // test octal/hex unescape.
        for ( int i = 0; i < 256; i++ ){
            buf.setLength(0);
            buf.append("c").append((char)i);
            jsonObj.clear();
            jsonObj.put(String.format("a\\%o", i), 0);
            jsonObj.put(String.format("b\\x%02X", i), 0);
            jsonObj.put(buf, 0);                            // raw.
            json = JSONUtil.toJSON(jsonObj, cfg);
            validateJSON(json);

            String r = JSONUtil.isValidIdentifierPart(i, cfg) ? String.format("%c", (char)i) : String.format("\\u%04X", i);

            assertThat(json, is("{\"a"+r+"\":0,\"b"+r+"\":0,\"c"+r+"\":0}"));
        }

        int maxLen = 512;
        buf.setLength(0);
        buf.append("c");
        cmpBuf.setLength(0);
        cmpBuf.append("{\"c");
        for ( int i = 256, ct = 0; i <= Character.MAX_CODE_POINT; i++ ){
            if ( isNormalCodePoint(i) ){
                buf.appendCodePoint(i);
                addCmp(i, cmpBuf, cfg);
                ++ct;
            }
            if ( ct % maxLen == 0 || i == Character.MAX_CODE_POINT ){
                jsonObj.clear();
                jsonObj.put(buf, 0);                            // raw.
                json = JSONUtil.toJSON(jsonObj, cfg);
                validateJSON(json);

                cmpBuf.append("\":0}");
                assertThat(json, is(cmpBuf.toString()));

                buf.setLength(0);
                buf.append("c");
                cmpBuf.setLength(0);
                cmpBuf.append("{\"c");
            }
        }

        cfg.setFullJSONIdentifierCodePoints(true);

        // test octal/hex unescape.
        for ( int i = 0; i < 256; i++ ){
            buf.setLength(0);
            buf.append("c").append((char)i);
            jsonObj.clear();
            jsonObj.put(String.format("a\\%o", i), 0);
            jsonObj.put(String.format("b\\x%02X", i), 0);
            jsonObj.put(buf, 0);                            // raw.
            json = JSONUtil.toJSON(jsonObj, cfg);
            validateJSON(json);

            String r = StringProcessor.getEscape((char)i);
            if ( r == null ){
                r = JSONUtil.isValidIdentifierPart(i, cfg) ? String.format("%c", (char)i) : String.format("\\u%04X", i);
            }

            assertThat(json, is("{\"a"+r+"\":0,\"b"+r+"\":0,\"c"+r+"\":0}"));
        }

        buf.setLength(0);
        buf.append("c");
        for ( int i = 256, ct = 0; i <= Character.MAX_CODE_POINT; i++ ){
            if ( isNormalCodePoint(i) ){
                buf.appendCodePoint(i);
                addCmp(i, cmpBuf, cfg);
                ++ct;
            }
            if ( ct % maxLen == 0 || i == Character.MAX_CODE_POINT ){
                jsonObj.clear();
                jsonObj.put(buf, 0);
                json = JSONUtil.toJSON(jsonObj, cfg);
                parseJSON(json);

                cmpBuf.append("\":0}");
                assertThat(json, is(cmpBuf.toString()));

                buf.setLength(0);
                buf.append("c");
                cmpBuf.setLength(0);
                cmpBuf.append("{\"c");
            }
        }
    }

    /**
     * Append the expected comparison data for the given code point
     * to the compare buffer.
     *
     * @param i the code point.
     * @param cmpBuf the compare buffer.
     * @param cfg the config object.
     */
    private void addCmp( int i, StringBuilder cmpBuf, JSONConfig cfg )
    {
        if ( JSONUtil.isValidIdentifierPart(i, cfg) ){
            cmpBuf.appendCodePoint(i);
        }else if ( i <= 0xFFFF ){
            cmpBuf.append(String.format("\\u%04X", i));
        }else{
            cmpBuf.append(String.format("\\u%04X\\u%04X", (int)Character.highSurrogate(i), (int)Character.lowSurrogate(i)));
        }
    }

    /**
     * Test setting default locale.
     */
    @Test
    public void testDefaultLocale()
    {
        Locale loc = new Locale("es","JP");
        Locale oldDefLoc = JSONConfigDefaults.getLocale();
        JSONConfigDefaults.setLocale(loc);
        Locale defLoc = JSONConfigDefaults.getLocale();
        JSONConfigDefaults.setLocale(oldDefLoc);
        assertEquals("Default locale not set", defLoc, loc);
        assertNotEquals(oldDefLoc, loc);
    }

    /**
     * Test dates.
     *
     * @throws ScriptException if the JSON doesn't evaluate properly.
     * @throws NoSuchMethodException If it can't find the Javascript function to use for validation.
     */
    @Test
    public void testDate() throws ScriptException, NoSuchMethodException
    {
        JSONConfig cfg = new JSONConfig();

        // non-standard JSON - only works with eval() and my parser.
        cfg.setEncodeDatesAsObjects(true);
        Map<String,Object> jsonObj = new LinkedHashMap<>();
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(2015, 8, 16, 14, 8, 34);
        cal.set(Calendar.MILLISECOND, 34);
        jsonObj.put("t", cal.getTime());
        String json = JSONUtil.toJSON(jsonObj, cfg);
        assertThat(json, is("{\"t\":new Date(\"2015-09-16T14:08:34.034Z\")}"));

        // examine the Javascript object created by this JSON and eval().
        Object result = evalJSON(json);
        if ( result instanceof ScriptObjectMirror ){
            ScriptObjectMirror mirror = (ScriptObjectMirror)result;
            Object obj = mirror.get("t");
            if ( obj instanceof ScriptObjectMirror ){
                ScriptObjectMirror t = (ScriptObjectMirror)obj;
                if ( t.getClassName().equals("Date") ){
                    assertEquals(new Double(2015), t.callMember("getUTCFullYear"));
                    assertEquals(new Double(8), t.callMember("getUTCMonth"));
                    assertEquals(new Double(16), t.callMember("getUTCDate"));
                    assertEquals(new Double(14), t.callMember("getUTCHours"));
                    assertEquals(new Double(8), t.callMember("getUTCMinutes"));
                    assertEquals(new Double(34), t.callMember("getUTCSeconds"));
                    assertEquals(new Double(34), t.callMember("getUTCMilliseconds"));
                }else{
                    fail("Expected Date from t");
                }
            }else{
                fail("Expected ScriptObjectMirror from result");
            }
        }else{
            fail("Expected ScriptObjectMirror from evalJSON");
        }

        cfg.setEncodeDatesAsStrings(true);
        json = JSONUtil.toJSON(jsonObj, cfg);
        validateJSON(json);
        assertThat(json, is("{\"t\":\"2015-09-16T14:08:34.034Z\"}"));
    }

    /**
     * Test booleans.
     *
     * @throws ScriptException if the JSON doesn't evaluate properly.
     * @throws NoSuchMethodException If it can't find the Javascript function to use for validation.
     */
    @Test
    public void testBoolean() throws ScriptException, NoSuchMethodException
    {
        Map<String,Object> jsonObj = new LinkedHashMap<>();
        jsonObj.put("t", true);
        jsonObj.put("f", false);
        String json = JSONUtil.toJSON(jsonObj);
        validateJSON(json);
        assertThat(json, is("{\"t\":true,\"f\":false}"));
    }

    /**
     * Test a byte value.
     *
     * @throws ScriptException if the JSON doesn't evaluate properly.
     * @throws NoSuchMethodException If it can't find the Javascript function to use for validation.
     */
    @Test
    public void testByte() throws ScriptException, NoSuchMethodException
    {
        Map<String,Object> jsonObj = new HashMap<>();
        byte b = 27;
        jsonObj.put("x", b);
        String json = JSONUtil.toJSON(jsonObj);
        validateJSON(json);
        assertThat(json, is("{\"x\":27}"));
    }

    /**
     * Test a char value.
     *
     * @throws ScriptException if the JSON doesn't evaluate properly.
     * @throws NoSuchMethodException If it can't find the Javascript function to use for validation.
     */
    @Test
    public void testChar() throws ScriptException, NoSuchMethodException
    {
        Map<String,Object> jsonObj = new HashMap<>();
        char ch = '@';
        jsonObj.put("x", ch);
        String json = JSONUtil.toJSON(jsonObj);
        validateJSON(json);
        assertThat(json, is("{\"x\":\"@\"}"));
    }

    /**
     * Test a short value.
     *
     * @throws ScriptException if the JSON doesn't evaluate properly.
     * @throws NoSuchMethodException If it can't find the Javascript function to use for validation.
     */
    @Test
    public void testShort() throws ScriptException, NoSuchMethodException
    {
        Map<String,Object> jsonObj = new HashMap<>();
        short s = 275;
        jsonObj.put("x", s);
        String json = JSONUtil.toJSON(jsonObj);
        validateJSON(json);
        assertThat(json, is("{\"x\":275}"));
    }

    /**
     * Test a int value.
     *
     * @throws ScriptException if the JSON doesn't evaluate properly.
     * @throws NoSuchMethodException If it can't find the Javascript function to use for validation.
     */
    @Test
    public void testInt() throws ScriptException, NoSuchMethodException
    {
        Map<String,Object> jsonObj = new HashMap<>();
        int i = 100000;
        jsonObj.put("x", i);
        String json = JSONUtil.toJSON(jsonObj);
        validateJSON(json);
        assertThat(json, is("{\"x\":100000}"));
    }

    /**
     * Test a byte value.
     *
     * @throws ScriptException if the JSON doesn't evaluate properly.
     * @throws NoSuchMethodException If it can't find the Javascript function to use for validation.
     */
    @Test
    public void testLong() throws ScriptException, NoSuchMethodException
    {
        Map<String,Object> jsonObj = new HashMap<>();
        long l = 68719476735L;
        jsonObj.put("x", l);
        String json = JSONUtil.toJSON(jsonObj);
        validateJSON(json);
        assertThat(json, is("{\"x\":68719476735}"));

        l = 9007199254740993L;
        jsonObj.put("x", l);
        JSONConfig cfg = new JSONConfig();

        cfg.setPreciseNumbers(false);
        json = JSONUtil.toJSON(jsonObj, cfg);
        assertThat(json, is("{\"x\":9007199254740993}"));

        cfg.setPreciseNumbers(true);
        json = JSONUtil.toJSON(jsonObj, cfg);
        assertThat(json, is("{\"x\":\"9007199254740993\"}"));
    }

    /**
     * Test a float value.
     *
     * @throws ScriptException if the JSON doesn't evaluate properly.
     * @throws NoSuchMethodException If it can't find the Javascript function to use for validation.
     */
    @Test
    public void testFloat() throws ScriptException, NoSuchMethodException
    {
        Map<String,Object> jsonObj = new HashMap<>();
        float f = 3.14f;
        jsonObj.put("x", f);
        String json = JSONUtil.toJSON(jsonObj);
        validateJSON(json);
        assertThat(json, is("{\"x\":3.14}"));
    }

    /**
     * Test a double value.
     *
     * @throws ScriptException if the JSON doesn't evaluate properly.
     * @throws NoSuchMethodException If it can't find the Javascript function to use for validation.
     */
    @Test
    public void testDouble() throws ScriptException, NoSuchMethodException
    {
        Map<String,Object> jsonObj = new HashMap<>();
        double d = 6.28;
        jsonObj.put("x", d);
        String json = JSONUtil.toJSON(jsonObj);
        validateJSON(json);
        assertThat(json, is("{\"x\":6.28}"));
    }

    /**
     * Test a BigInteger value.
     *
     * @throws ScriptException if the JSON doesn't evaluate properly.
     * @throws NoSuchMethodException If it can't find the Javascript function to use for validation.
     */
    @Test
    public void testBigInteger() throws ScriptException, NoSuchMethodException
    {
        Map<String,Object> jsonObj = new HashMap<>();
        BigInteger bi = new BigInteger("1234567890");
        jsonObj.put("x", bi);
        String json = JSONUtil.toJSON(jsonObj);
        validateJSON(json);
        assertThat(json, is("{\"x\":1234567890}"));

        bi = new BigInteger("9007199254740993");
        jsonObj.put("x", bi);
        JSONConfig cfg = new JSONConfig();

        cfg.setPreciseNumbers(false);
        json = JSONUtil.toJSON(jsonObj, cfg);
        assertThat(json, is("{\"x\":9007199254740993}"));

        cfg.setPreciseNumbers(true);
        json = JSONUtil.toJSON(jsonObj, cfg);
        assertThat(json, is("{\"x\":\"9007199254740993\"}"));
    }

    /**
     * Test a BigDecimal value.
     *
     * @throws ScriptException if the JSON doesn't evaluate properly.
     * @throws NoSuchMethodException If it can't find the Javascript function to use for validation.
     */
    @Test
    public void testBigDecimal() throws ScriptException, NoSuchMethodException
    {
        Map<String,Object> jsonObj = new HashMap<>();
        BigDecimal bd = new BigDecimal("12345.67890");
        jsonObj.put("x", bd);
        String json = JSONUtil.toJSON(jsonObj);
        validateJSON(json);
        assertThat(json, is("{\"x\":12345.67890}"));

        bd = new BigDecimal("9007199254740993");
        jsonObj.put("x", bd);
        JSONConfig cfg = new JSONConfig();

        cfg.setPreciseNumbers(false);
        json = JSONUtil.toJSON(jsonObj, cfg);
        assertThat(json, is("{\"x\":9007199254740993}"));

        cfg.setPreciseNumbers(true);
        json = JSONUtil.toJSON(jsonObj, cfg);
        assertThat(json, is("{\"x\":\"9007199254740993\"}"));
    }

    /**
     * Test a custom number format.
     *
     * @throws ScriptException if the JSON doesn't evaluate properly.
     * @throws NoSuchMethodException If it can't find the Javascript function to use for validation.
     */
    @Test
    public void testNumberFormat() throws ScriptException, NoSuchMethodException
    {
        Map<String,Object> jsonObj = new HashMap<>();
        float f = 1.23456f;
        jsonObj.put("x", f);
        NumberFormat fmt = NumberFormat.getInstance();
        fmt.setMaximumFractionDigits(3);
        JSONConfig cfg = new JSONConfig().addNumberFormat(f, fmt);
        String json = JSONUtil.toJSON(jsonObj, cfg);
        validateJSON(json);
        assertThat(json, is("{\"x\":1.235}"));
    }

    /**
     * Test a string value.
     *
     * @throws ScriptException if the JSON doesn't evaluate properly.
     * @throws NoSuchMethodException If it can't find the Javascript function to use for validation.
     */
    @Test
    public void testString() throws ScriptException, NoSuchMethodException
    {
        Map<String,Object> jsonObj = new HashMap<>();
        String s = "bar";
        jsonObj.put("x", s);
        String json = JSONUtil.toJSON(jsonObj);
        validateJSON(json);
        assertThat(json, is("{\"x\":\"bar\"}"));
    }

    /**
     * Test a string with a quote value.
     *
     * @throws ScriptException if the JSON doesn't evaluate properly.
     * @throws NoSuchMethodException If it can't find the Javascript function to use for validation.
     */
    @Test
    public void testQuoteString() throws ScriptException, NoSuchMethodException
    {
        Map<String,Object> jsonObj = new HashMap<>();
        String s = "ba\"r";
        jsonObj.put("x", s);
        String json = JSONUtil.toJSON(jsonObj);
        validateJSON(json);
        assertThat(json, is("{\"x\":\"ba\\\"r\"}"));
    }

    /**
     * Test a string with a quote value.
     *
     * @throws ScriptException if the JSON doesn't evaluate properly.
     * @throws NoSuchMethodException If it can't find the Javascript function to use for validation.
     */
    @Test
    public void testNonBmp() throws ScriptException, NoSuchMethodException
    {
        Map<String,Object> jsonObj = new HashMap<>();
        StringBuilder buf = new StringBuilder(2);
        buf.appendCodePoint(0x1F4A9);
        jsonObj.put("x", buf);
        String json = JSONUtil.toJSON(jsonObj);
        validateJSON(json);
        assertThat(json, is("{\"x\":\"\uD83D\uDCA9\"}"));
    }

    /**
     * Test a Iterable value.
     *
     * @throws ScriptException if the JSON doesn't evaluate properly.
     * @throws NoSuchMethodException If it can't find the Javascript function to use for validation.
     */
    @Test
    public void testIterable() throws ScriptException, NoSuchMethodException
    {
        Map<String,Object> jsonObj = new HashMap<>();
        jsonObj.put("x", Arrays.asList(1,2,3));
        String json = JSONUtil.toJSON(jsonObj);
        validateJSON(json);
        assertThat(json, is("{\"x\":[1,2,3]}"));
    }

    /**
     * Test an Enumeration.
     *
     * @throws ScriptException if the JSON doesn't evaluate properly.
     * @throws NoSuchMethodException If it can't find the Javascript function to use for validation.
     */
    @Test
    public void testEnumeration() throws ScriptException, NoSuchMethodException
    {
        Map<String,Object> jsonObj = new HashMap<>();

        Vector<Integer> list = new Vector<>(Arrays.asList(1,2,3));
        jsonObj.put("x", list.elements());
        String json = JSONUtil.toJSON(jsonObj);
        validateJSON(json);
        assertThat(json, is("{\"x\":[1,2,3]}"));
    }

    /**
     * Test a Map value.
     *
     * @throws ScriptException if the JSON doesn't evaluate properly.
     */
    @Test
    public void testLoop() throws ScriptException
    {
        Map<String,Object> jsonObj = new HashMap<>(4);
        jsonObj.put("a",1);
        jsonObj.put("b",2);
        jsonObj.put("c",3);
        jsonObj.put("x", jsonObj);

        JSONConfig cfg = new JSONConfig();
        try{
            JSONUtil.toJSON(jsonObj, cfg);
            fail("Expected a DataStructureLoopException to be thrown");
        }catch ( DataStructureLoopException e ){
            assertThat(e.getMessage(), containsString("java.util.HashMap includes itself which would cause infinite recursion."));
        }
    }

    /**
     * Test a resource bundle.
     *
     * @throws ScriptException if the JSON doesn't evaluate properly.
     * @throws NoSuchMethodException If it can't find the Javascript function to use for validation.
     */
    @Test
    public void testResourceBundle() throws ScriptException, NoSuchMethodException
    {
        String bundleName = getClass().getCanonicalName();
        ResourceBundle bundle = ResourceBundle.getBundle(bundleName);
        JSONConfig cfg = new JSONConfig();

        cfg.setEncodeNumericStringsAsNumbers(false);
        String json = JSONUtil.toJSON(bundle, cfg);
        validateJSON(json);
        assertThat(json, is("{\"a\":\"1\",\"b\":\"2\",\"c\":\"3\",\"d\":\"4\",\"e\":\"5\",\"f\":\"6\",\"g\":\"7\",\"h\":\"8\",\"i\":\"9\",\"j\":\"10\",\"k\":\"11\",\"l\":\"12\",\"m\":\"13\",\"n\":\"14\",\"o\":\"15\",\"p\":\"16\",\"q\":\"17\",\"r\":\"18\",\"s\":\"19\",\"t\":\"20\",\"u\":\"21\",\"v\":\"22\",\"w\":\"23\",\"x\":\"24\",\"y\":\"25\",\"z\":\"26\"}"));

        cfg.setEncodeNumericStringsAsNumbers(true);
        json = JSONUtil.toJSON(bundle, cfg);
        validateJSON(json);
        assertThat(json, is("{\"a\":1,\"b\":2,\"c\":3,\"d\":4,\"e\":5,\"f\":6,\"g\":7,\"h\":8,\"i\":9,\"j\":10,\"k\":11,\"l\":12,\"m\":13,\"n\":14,\"o\":15,\"p\":16,\"q\":17,\"r\":18,\"s\":19,\"t\":20,\"u\":21,\"v\":22,\"w\":23,\"x\":24,\"y\":25,\"z\":26}"));
    }

    /**
     * Test a complex value.
     *
     * @throws ScriptException if the JSON doesn't evaluate properly.
     * @throws NoSuchMethodException If it can't find the Javascript function to use for validation.
     */
    @Test
    public void testComplex() throws ScriptException, NoSuchMethodException
    {
        Map<String,Object> jsonObj = new LinkedHashMap<>();
        jsonObj.put("a",1);
        jsonObj.put("b","x");
        String[] ia = {"1","2","3"};
        List<String> il = Arrays.asList(ia);
        jsonObj.put("c",ia);
        jsonObj.put("d",il);
        Object[] objs = new Object[3];
        objs[0] = null;
        objs[1] = (JSONAble)(jsonConfig, json) ->
                      {
                          JSONConfig cfg = jsonConfig == null ? new JSONConfig() : jsonConfig;
                          Map<String,Object> stuff = new LinkedHashMap<>();
                          stuff.put("a", 0);
                          stuff.put("b", 2);
                          int[] ar = {1, 2, 3};
                          stuff.put("x", ar);
                          stuff.put("t", null);

                          JSONUtil.toJSON(stuff, cfg, json);
                     };
        objs[2] = il;
        jsonObj.put("e", objs);

        JSONConfig cfg = new JSONConfig();
        String json = JSONUtil.toJSON(jsonObj, cfg);
        validateJSON(json);
        assertThat(json, is("{\"a\":1,\"b\":\"x\",\"c\":[\"1\",\"2\",\"3\"],\"d\":[\"1\",\"2\",\"3\"],\"e\":[null,{\"a\":0,\"b\":2,\"x\":[1,2,3],\"t\":null},[\"1\",\"2\",\"3\"]]}"));
    }

    /**
     * Test indenting.
     *
     * @throws ScriptException if the JSON doesn't evaluate properly.
     * @throws NoSuchMethodException If it can't find the Javascript function to use for validation.
     */
    @Test
    public void testIndent() throws NoSuchMethodException, ScriptException
    {
        Map<String,Object> jsonObj = new LinkedHashMap<>();
        jsonObj.put("a",1);
        jsonObj.put("b","x");
        String[] ia = {"1","2","3"};
        List<String> il = Arrays.asList(ia);
        jsonObj.put("c",ia);
        jsonObj.put("d",il);
        Object[] objs = new Object[3];
        objs[0] = null;
        objs[1] = (JSONAble)(jsonConfig, json) ->
                      {
                          JSONConfig cfg = jsonConfig == null ? new JSONConfig() : jsonConfig;
                          Map<String,Object> stuff = new LinkedHashMap<>();
                          Map<String,Object> more = new LinkedHashMap<>();
                          stuff.put("a", 0);
                          stuff.put("b", 2);
                          int[] ar = {1, 2, 3};
                          stuff.put("x", ar);
                          more.put("z", 4);
                          more.put("y", 2);
                          more.put("t", null);
                          stuff.put("w", more);

                          JSONUtil.toJSON(stuff, cfg, json);
                     };
        objs[2] = il;
        jsonObj.put("e", objs);

        JSONConfig cfg = new JSONConfig().setIndentPadding(new IndentPadding("\t", String.format("%n")));
        String json = JSONUtil.toJSON(jsonObj, cfg);
        validateJSON(json);
        @SuppressWarnings("unchecked")
        Map<String,Object> obj = (Map<String,Object>)JSONParser.parseJSON(json);
        assertThat((String)obj.get("b"), is("x"));
        @SuppressWarnings("unchecked")
        ArrayList<Object> innerObj = (ArrayList<Object>)obj.get("e");
        @SuppressWarnings("unchecked")
        Map<Object,Object> jsonAble = (Map<Object,Object>)innerObj.get(1);
        assertThat(((Number)jsonAble.get("b")).intValue(), is(2));
        //System.out.println(json);
    }

    /**
     * Return true if the character is defined and not a surrogate.
     *
     * @param codePoint The code point to check.
     * @return true if the character is defined and not a surrogate.
     */
    private boolean isNormalCodePoint( int codePoint )
    {
        if ( Character.isDefined(codePoint) ){
            if ( codePoint <= 0xFFFF && Character.isSurrogate((char)codePoint) ){
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * Test the oddball case where a map has keys which are not equal
     * but produce the same toString() output.
     */
    @Test
    public void testDuplicateKeys()
    {
        JsonObject jsonObj = new JsonObject(2)
                                    .add(new DupStr(1), 0)
                                    .add(new DupStr(2), 1);
        try{
            jsonObj.toJSON();
            fail("Expected a DuplicatePropertyNameException to be thrown");
        }catch ( DuplicatePropertyNameException e ){
            assertThat(e.getMessage(), is("Property x occurs twice in the same object."));
        }
    }

    /**
     * Used by testDuplicateKeys().
     */
    private class DupStr
    {
        private int x;

        public DupStr( int x )
        {
            this.x = x;
        }

        @Override
        public String toString()
        {
            return "x";
        }

        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + getOuterType().hashCode();
            result = prime * result + x;
            return result;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals( Object obj )
        {
            if ( this == obj ){
                return true;
            }
            if ( obj == null ){
                return false;
            }
            if ( getClass() != obj.getClass() ){
                return false;
            }
            DupStr other = (DupStr)obj;
            if ( !getOuterType().equals(other.getOuterType()) ){
                return false;
            }
            if ( x != other.x ){
                return false;
            }
            return true;
        }

        private TestJSONUtil getOuterType()
        {
            return TestJSONUtil.this;
        }
    }

    /**
     * Test reflection.
     */
    @Test
    public void testReflect()
    {
        JSONConfig cfg = new JSONConfig().setValidatePropertyNames(false)
                                         .setDetectDataStructureLoops(false)
                                         .setFastStrings(true)
                                         .setCacheReflectionData(false);
        JsonObject jsonObj = new JsonObject(1, cfg);
        jsonObj.add("f", new ReflectTestClass());

        ReflectedObjectMapBuilder.clearReflectionCache();

        // JNDI set up to only show fields a and e.
        String json = jsonObj.toJSON();
        assertThat(json, is("{\"f\":{\"a\":1,\"k\":25.0}}"));

        cfg.clearReflectClasses();
        cfg.addReflectClass(ReflectTestClass.class);

        cfg.setReflectionPrivacy(ReflectUtil.PRIVATE);
        json = jsonObj.toJSON();
        assertThat(json, is("{\"f\":{\"a\":1,\"b\":\"something\",\"c\":[],\"d\":null,\"f\":true}}"));

        cfg.setReflectionPrivacy(ReflectUtil.PACKAGE);
        json = jsonObj.toJSON();
        assertThat(json, is("{\"f\":{\"a\":1,\"b\":\"something\",\"c\":[],\"f\":true}}"));

        cfg.setReflectionPrivacy(ReflectUtil.PROTECTED);
        json = jsonObj.toJSON();
        assertThat(json, is("{\"f\":{\"a\":1,\"b\":\"something\",\"f\":true}}"));

        cfg.setReflectionPrivacy(ReflectUtil.PUBLIC);
        json = jsonObj.toJSON();
        assertThat(json, is("{\"f\":{\"a\":1,\"f\":true}}"));

        cfg = new JSONConfig(); // reload defaults.
        jsonObj.setJSONConfig(cfg);
        json = jsonObj.toJSON();
        assertThat(json, is("{\"f\":{\"a\":1,\"k\":25.0}}"));

        JSONConfigDefaults.getInstance().clearReflectClasses();
        ReflectedObjectMapBuilder.clearReflectionCache();
        cfg.setReflectUnknownObjects(false)
           .setReflectionPrivacy(ReflectUtil.PRIVATE)
           .addReflectClass(ReflectTestClass.class);

        //int iterations = 1000000;
        //int iterations = 100000;
        int iterations = 100;
        //int iterations = 0;

        /*
         * These timings get a little funky due to the way that the JVM cache's
         * things and improves its performance after doing the same thing
         * repeatedly.  Initially, it seems like maps are slow but eventually, as
         * the JVM does its optimization thing, they are faster.
         */
        ReflectTestClass r = new ReflectTestClass();
        runMapTiming(iterations, r, cfg);
        runReflectionTiming(iterations, r, cfg, false);
        runReflectionTiming(iterations, r, cfg, true);

        runMapTiming(iterations, r, cfg);
        runReflectionTiming(iterations, r, cfg, false);
        runReflectionTiming(iterations, r, cfg, true);

        // BigObject
        iterations = 1;

        cfg.clearReflectClasses()
           .addReflectClass(BigObject.class)
           .setFastStrings(false)
           .setEscapeNonAscii(true)
           .setUseECMA6(false);
        BigObject bigObj = new BigObject();

        runReflectionTiming(iterations, bigObj, cfg, false);
        runReflectionTiming(iterations, bigObj, cfg, true);

        runReflectionTiming(iterations, bigObj, cfg, false);
        runReflectionTiming(iterations, bigObj, cfg, true);
    }

    private String runMapTiming( int iterations, ReflectTestClass obj, JSONConfig cfg )
    {
        JsonObject jsonObj = new JsonObject(1, cfg);
        String json = null;

        long start = System.currentTimeMillis();
        for ( int i = 0; i < iterations; i++ ){
            JsonObject mapObj = new JsonObject(4, cfg)
                                        .add("a", obj.getA())
                                        .add("b", obj.getB())
                                        .add("c", obj.getC())
                                        .add("d", null);  // no getter and private
            jsonObj.clear();
            jsonObj.add("f", mapObj);
            json = jsonObj.toJSON();
        }
        long end = System.currentTimeMillis();
        s_log.debug("map: "+((end-start)/1000.0)+"s");
        return json;
    }

    private String runReflectionTiming( int iterations, Object obj, JSONConfig cfg, boolean doCaching )
    {
        JsonObject jsonObj = new JsonObject(1, cfg).add("f", obj);
        String json = null;

        String tag;
        if ( doCaching ){
            ReflectedObjectMapBuilder.clearReflectionCache();
            cfg.setCacheReflectionData(true);
            tag = "cached: ";
        }else{
            cfg.setCacheReflectionData(false);
            tag = "uncached: ";
        }
        long start = System.currentTimeMillis();
        for ( int i = 0; i < iterations; i++ ){
            json = jsonObj.toJSON();
        }
        long end = System.currentTimeMillis();
        s_log.debug(tag+((end-start)/1000.0)+"s");
        if ( doCaching ){
            ReflectedObjectMapBuilder.clearReflectionCache();
        }
        return json;
    }
}
