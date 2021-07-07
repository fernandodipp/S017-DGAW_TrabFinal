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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import javax.management.MBeanException;

/**
 * MBean interface for JSONConfigDefaults to expose its methods to view and
 * modify the defaults at run time when this library is used with an MBean
 * server.
 *
 * @author Bill Davidson
 */
public interface JSONConfigDefaultsMBean
{
    /**
     * Reset all defaults to their original unmodified values.  This
     * overrides JNDI and previous MBean changes.
     */
    public void setCodeDefaults();

    /**
     * Get the default locale for new {@link JSONConfig} objects in string form.
     *
     * @return The string form of the default locale.
     */
    public String getLocaleLanguageTag();

    /**
     * Set the default locale for new {@link JSONConfig} objects to use.
     *
     * @param languageTag A language tag suitable for use by {@link Locale#forLanguageTag(String)}.
     */
    public void setLocaleLanguageTag( String languageTag );

    /**
     * Clear any default number formats.
     *
     * @since 1.4
     */
    public void clearNumberFormats();

    /**
     * Set the date format used for date string generation when
     * encodeDatesAsStrings or encodeDatesAsObjects is true.
     *
     * @param fmtStr passed to the constructor for
     * {@link SimpleDateFormat#SimpleDateFormat(String,Locale)} using
     * the result of {@link JSONConfigDefaults#getLocale()}.
     * @return the format that is created.
     * @since 1.4
     */
    public DateFormat setDateGenFormat( String fmtStr );

    /**
     * Clear date generation format.
     *
     * @since 1.4
     */
    public void clearDateGenFormat();

    /**
     * Add a date parsing format to the list of date parsing formats
     * used by the parser when encodeDatesAsStrings or
     * encodeDatesAsObjects is true.
     *
     * @param fmtStr Passed to
     * {@link SimpleDateFormat#SimpleDateFormat(String,Locale)} using
     * the result of {@link JSONConfigDefaults#getLocale()}.
     * @return The format that gets created.
     * @since 1.4
     */
    public DateFormat addDateParseFormat( String fmtStr );

    /**
     * Clear any date parse formats from the list of formats
     * used by the parser when encodeDatesAsStrings or
     * encodeDatesAsObjects is true.
     */
    public void clearDateParseFormats();

    /**
     * Get the default validate property names policy.
     *
     * @return The default validate property names policy.
     */
    public boolean isValidatePropertyNames();

    /**
     * Get the reflection privacy level.
     *
     * @return the reflection privacy level.
     * @since 1.9
     */
    public int getReflectionPrivacy();

    /**
     * Set the privacy level for reflection. Default is
     * {@link ReflectUtil#PRIVATE} which includes all fields when reflection is
     * enabled.
     *
     * @param dflt the level to set
     * @throws MBeanException If the privacy level is not allowed.
     * @see ReflectUtil#PRIVATE
     * @see ReflectUtil#PACKAGE
     * @see ReflectUtil#PROTECTED
     * @see ReflectUtil#PUBLIC
     * @since 1.9
     */
    public void setReflectionPrivacy( int dflt ) throws MBeanException;

    /**
     * Clear all reflection classes, disabling all default automatic reflection.
     *
     * @since 1.9
     */
    public void clearReflectClasses();

    /**
     * Clear the reflection cache.
     */
    public void clearReflectionCache();

    /**
     * Add the given class to the set of classes to be reflected.
     *
     * @param className The name of the class suitable for
     * (@link {@link ClassLoader#loadClass(String)}}.
     * @throws MBeanException If there's a problem loading the class.
     * @since 1.9
     */
    public void addReflectClassByName( String className ) throws MBeanException;

    /**
     * Remove the given class from the set of classes to be reflected.
     *
     * @param className The name of the class suitable for
     * (@link {@link ClassLoader#loadClass(String)}}.
     * @throws MBeanException If there's a problem loading the class.
     * @since 1.9
     */
    public void removeReflectClassByName( String className ) throws MBeanException;

    /**
     * Get a string with newline separated list of classes that get reflected.
     *
     * @return A string with newline separated list of classes that get reflected.
     * @since 1.9
     */
    public String listReflectedClasses();

    /**
     * Get the default policy for unmatched surrogates.
     *
     * @return the default policy for unmatched surrogates.
     */
    public int getUnmatchedSurrogatePolicy();

    /**
     * Tell JSONUtil what to do by default when it encounters unmatched surrogates in strings
     * and identifiers.  The permitted values are:
     * <ul>
     *   <li>{@link JSONConfig#REPLACE} - Replace with Unicode replacement character U+FFFD (default)</li>
     *   <li>{@link JSONConfig#DISCARD} - Discard them.</li>
     *   <li>{@link JSONConfig#EXCEPTION} - Throw a {@link UndefinedCodePointException}</li>
     *   <li>{@link JSONConfig#ESCAPE} - Include them but escape them</li>
     *   <li>{@link JSONConfig#PASS} - Pass them through unmodified.</li>
     * </ul>
     * Any other value will be ignored.
     *
     * @param dflt the default unmatchedSurrogatePolicy to set
     */
    public void setUnmatchedSurrogatePolicy( int dflt );

    /**
     * Get the default policy for undefined code points.
     *
     * @return the policy for undefined code points.
     */
    public int getUndefinedCodePointPolicy();

    /**
     * Tell JSONUtil what to do by default when it encounters undefined code points in strings
     * and identifiers.  The permitted values are:
     * <ul>
     *   <li>{@link JSONConfig#REPLACE} - Replace with Unicode replacement character U+FFFD (default)</li>
     *   <li>{@link JSONConfig#DISCARD} - Discard them.</li>
     *   <li>{@link JSONConfig#EXCEPTION} - Throw a {@link UndefinedCodePointException}</li>
     *   <li>{@link JSONConfig#ESCAPE} - Include them but escape them</li>
     *   <li>{@link JSONConfig#PASS} - Pass them through unmodified.</li>
     * </ul>
     * Any other value will be ignored.
     *
     * @param dflt the default undefinedCodePointPolicy to set
     */
    public void setUndefinedCodePointPolicy( int dflt );

    /**
     * Set the default flag for validation of property names.
     * This will affect all new {@link JSONConfig} objects created after this call
     * within the same class loader.
     *
     * @param dflt If true, then property names will be validated by default.
     */
    public void setValidatePropertyNames( boolean dflt );

    /**
     * Get the default detect data structure loops policy.
     * Accessible via MBean server.
     *
     * @return The default detect data structure loops policy.
     */
    public boolean isDetectDataStructureLoops();

    /**
     * Set the default flag for detecting data structure loops.  If true,
     * then if a loop in a data structure is found then a
     * {@link DataStructureLoopException} will be thrown.
     * This will affect all new {@link JSONConfig} objects created after this call
     * within the same class loader.
     *
     * @param dflt If true, then the code will detect loops in data structures.
     */
    public void setDetectDataStructureLoops( boolean dflt );

    /**
     * Get the default escape bad identifier code points policy.
     *
     * @return The default escape bad identifier code points policy.
     */
    public boolean isEscapeBadIdentifierCodePoints();

    /**
     * If true, then any bad code points in identifiers will be escaped.
     * Default is false.
     *
     * @param dflt if true, then any bad code points in identifiers will be escaped.
     */
    public void setEscapeBadIdentifierCodePoints( boolean dflt );

    /**
     * Get the full JSON identifier code points policy.
     *
     * @return the fullJSONIdentifierCodePoints
     */
    public boolean isFullJSONIdentifierCodePoints();

    /**
     * If true, then the full set of identifier code points permitted by the
     * JSON standard will be allowed instead of the more restrictive set
     * permitted by the ECMAScript standard. Use of characters not permitted by
     * the ECMAScript standard will cause an error if parsed by Javascript
     * eval().
     *
     * @param dflt If true, then allow all code points permitted by the JSON standard in identifiers.
     */
    public void setFullJSONIdentifierCodePoints( boolean dflt );

    /**
     * Get the fastStrings policy.
     *
     * @return the fastStrings policy
     */
    public boolean isFastStrings();

    /**
     * If true, then string values will be copied to the output with no escaping
     * or validation.
     * <p>
     * Only use this if you know that you have no characters in the range
     * U+0000-U+001F or backslash or forward slash or double quote in your
     * strings. If you want your JSON to be parsable by Javascript eval() then
     * you also need to make sure that you don't have U+2028 (line separator) or
     * U+2029 (paragraph separator).
     * <p>
     * That said, if you are encoding a lot of large strings, this can
     * improve performance by eliminating the check for characters that need
     * to be escaped.
     *
     * @param dflt If true, then strings will be copied as is with no escaping
     *            or validation.
     */
    public void setFastStrings( boolean dflt );

    /**
     * Get the default encode numeric strings as numbers policy.
     *
     * @return The default encode numeric strings as numbers policy.
     */
    public boolean isEncodeNumericStringsAsNumbers();

    /**
     * Set the default flag for encoding of numeric strings as numbers.
     *
     * @param dflt If true, then strings that look like valid JSON numbers
     * will be encoded as numbers.
     */
    public void setEncodeNumericStringsAsNumbers( boolean dflt );

    /**
     * Get the default escape non-ASCII policy.
     *
     * @return The default quote non-ASCII policy.
     */
    public boolean isEscapeNonAscii();

    /**
     * Set the default flag for forcing escaping of non-ASCII characters in
     * strings and identifiers. If true, then escapeSurrogates will be forced to
     * false. This will affect all new {@link JSONConfig} objects created after this
     * call within the same class loader.
     *
     * @param dflt If true, then all non-ASCII will be Unicode escaped.
     */
    public void setEscapeNonAscii( boolean dflt );

    /**
     * The default unEscape policy.
     *
     * @return the unEscape policy.
     */
    public boolean isUnEscapeWherePossible();

    /**
     * Set default flag for undoing inline escapes in strings.
     *
     * @param dflt If true then where possible, undo inline escapes in strings.
     */
    public void setUnEscapeWherePossible( boolean dflt );

    /**
     * Get the default escape surrogates policy.
     *
     * @return the escape surrogates policy.
     */
    public boolean isEscapeSurrogates();

    /**
     * Set the default escapeSurrogates policy.
     *
     * @param dflt If true, then surrogates will be escaped in strings and identifiers
     * and escapeNonAscii will be forced to false.
     */
    public void setEscapeSurrogates( boolean dflt );

    /**
     * Get the pass through escapes policy.
     * <p>
     * Accessible via MBean server.
     *
     * @return The pass through escapes policy.
     */
    public boolean isPassThroughEscapes();

    /**
     * If true, then escapes in strings will be passed through unchanged.
     * If false, then the backslash that starts the escape will be escaped.
     * <p>
     * Accessible via MBean server.
     *
     * @param dflt If true, then pass escapes through.
     */
    public void setPassThroughEscapes( boolean dflt );

    /**
     * Get the encode dates as strings policy.
     *
     * @return the encodeDatesAsStrings policy.
     */
    public boolean isEncodeDatesAsStrings();

    /**
     * Set the encodeDatesAsStrings policy.  If you set this to true, then
     * encodeDatesAsObjects will be set to false.
     * <p>
     * Accessible via MBean server.
     *
     * @param dflt If true, then {@link Date} objects will be encoded as ISO 8601 date
     * strings or a custom date format if you have called
     * {@link JSONConfigDefaults#setDateGenFormat(DateFormat)}.
     */
    public void setEncodeDatesAsStrings( boolean dflt );

    /**
     * Get the reflection of unknown objects policy.
     *
     * @return the reflectUnknownObjects policy.
     * @since 1.9
     */
    public boolean isReflectUnknownObjects();

    /**
     * Set the reflection encoding policy.  If true, then any time that an
     * unknown object is encountered, this package will attempt to use
     * reflection to encode it.  Default is false.  When false, then unknown
     * objects will have their toString() method called.
     *
     * @param dflt If true, then attempt to use reflection
     * to encode objects which are otherwise unknown.
     * @since 1.9
     */
    public void setReflectUnknownObjects( boolean dflt );

    /**
     * Get the preciseFloatingPoint policy.
     *
     * @return The preciseFloatingPoint policy.
     * @since 1.9
     */
    public boolean isPreciseNumbers();

    /**
     * If true then numbers which are not exactly representable by a 64 bit
     * double precision floating point number will be quoted in the output. If
     * false, then they will be unquoted, and precision in such will likely be
     * lost in the interpreter.
     *
     * @param dflt If true then quote numbers that lose precision in 64-bit floating point.
     * @since 1.9
     */
    public void setPreciseNumbers( boolean dflt );

    /**
     * Get the smallNumbers policy.
     *
     * @return The smallNumbers policy.
     * @since 1.9
     */
    public boolean isSmallNumbers();

    /**
     * If true then {@link JSONParser} will attempt to minimize the
     * storage used for all numbers.  Decimal numbers will be reduced
     * to floats instead of doubles if it can done without losing
     * precision.  Integer numbers will be reduced from long to int
     * or short or byte if they fit.
     *
     * @param dflt If true then numbers will be made to use as little memory as possible.
     * @since 1.9
     */
    public void setSmallNumbers( boolean dflt );

    /**
     * The primitive arrays policy.
     *
     * @return the usePrimitiveArrays policy.
     * @since 1.9
     */
    public boolean isUsePrimitiveArrays();

    /**
     * If true, then when {@link JSONParser} encounters a JSON array of non-null
     * wrappers of primitives and those primitives are all compatible with each
     * other, then instead of an {@link ArrayList} of wrappers for those
     * primitives it will create an array of those primitives in order to save
     * memory.
     * <p>
     * This works for booleans and numbers. It will also convert an array of
     * single character strings into an array of chars. Arrays of numbers will
     * attempt to use the least complex type that does not lose information. You
     * could easily end up with an array of bytes if all of your numbers are
     * integers in the range -128 to 127. This option is meant to save as much
     * memory as possible.
     *
     * @param dflt if true, then the parser will create arrays of primitives as
     *            applicable.
     * @since 1.9
     */
    public void setUsePrimitiveArrays( boolean dflt );


    /**
     * Get the the cacheReflectionData policy.
     *
     * @return the cacheReflectionData policy.
     * @since 1.9
     */
    public boolean isCacheReflectionData();

    /**
     * If true, then when an object is reflected its reflection data
     * will be cached to improve performance on subsequent reflections
     * of objects of its class.
     *
     * @param dflt if true, then cache reflection data.
     * @since 1.9
     */
    public void setCacheReflectionData( boolean dflt );

    /**
     * Get the default quote identifier policy.
     *
     * @return The default quote identifier policy.
     */
    public boolean isQuoteIdentifier();

    /**
     * Set the default flag for forcing quotes on identifiers.
     * This will affect all new {@link JSONConfig} objects created after this call
     * within the same class loader.
     * <p>
     * Accessible via MBean server.
     *
     * @param dflt If true, then all identifiers will be quoted.
     */
    public void setQuoteIdentifier( boolean dflt );

    /**
     * Get the default escape ECMAScript 6 code points policy.
     *
     * @return The default escape ECMAScript 6 code points policy.
     */
    public boolean isUseECMA6();

    /**
     * If you set this to true, then when JSONUtil generates Unicode
     * escapes, it will use ECMAScript 6 code point escapes if they are shorter
     * than code unit escapes. This is not standard JSON and not yet widely
     * supported by Javascript interpreters. It also allows identifiers to have
     * letter numbers in addition to other letters.  Default is false.
     *
     * @param dflt If true, use EMCAScript 6 code point escapes and allow
     * ECMAScript 6 identifier character set.
     */
    public void setUseECMA6( boolean dflt );

    /**
     * Get the default for allowing reserved words in identifiers.
     *
     * @return the reserverd words in identifiers policy.
     */
    public boolean isAllowReservedWordsInIdentifiers();

    /**
     * Set default flag for allowing reserved words in identifiers.
     *
     * @param dflt If true, then reserved words will be allowed in identifiers.
     */
    public void setAllowReservedWordsInIdentifiers( boolean dflt );

    /**
     * Get the encode dates as objects policy.
     *
     * @return the encodeDatesAsObjects policy.
     */
    public boolean isEncodeDatesAsObjects();

    /**
     * If true, then {@link Date} objects will be encoded as
     * Javascript dates, using new Date(dateString).  If you
     * set this to true, then encodeDatesAsStrings will be
     * set to false.
     *
     * @param dflt If true, then {@link Date} objects will be encoded as
     * Javascript dates.
     */
    public void setEncodeDatesAsObjects( boolean dflt );
}
