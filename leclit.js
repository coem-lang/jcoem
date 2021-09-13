(function() {
  class LecLit {
    constructor() {
      console.log("LecLit initialised");
    }

    /**
     * Returns length of given string.
     * @param  {String} s String to find length of.
     * @return {number}   Length (number of characters) in string.
     */
    length(s) {
      return s.length;
    }

    /**
     * Return definition of given string.
     * @param  {String} s String to define.
     * @return {String}   Definition of string, if exists.
     */
    define(s) {

    }

    /**
     * Returns synonyms for given string.
     * @param  {String} s String to get synonyms for.
     * @return {Array}   Array of synonyms, if exists.
     * @see antonyms
     */
    synonyms(s) {

    }

    /**
     * Returns antonyms for given string.
     * @param  {String} s String to get antonyms for.
     * @return {Array}   Array of antonyms, if exists.
     * @see synonyms
     */
    synonyms(s) {

    }

    /**
     * Returns rhyming strings for given string.
     * @param  {String} s     String to get rhyming strings for.
     * @param  {Object} types Options for types of rhymes desired.
     * @return {Array}       Array of rhyming strings, if exists.
     */
    rhymes(s, types={perfect: true, slant: false, eye: false}) {

    }

    /**
     * Returns a scrambled version of the given string.
     * @param  {String}  s              String to scramble.
     * @param  {Boolean} preserveBreaks Whether the position of whitespace
     * characters should be preserved in the scrambled string.
     * @return {String}                 A scrambled version of the given
     * string.
     */
    scramble(s, preserveBreaks=true) {
      let shuffle = array => {
        array.sort(() => Math.random() - 0.5);
      }
      let scrambled;
      if (preserveBreaks) {
        let words = s.split(" ");
        for (let i = 0; i < words.length; i++) {
          words[i] = words[i].split("");
          shuffle(words[i]);
          words[i] = words[i].join("");
        }
        scrambled = words.join(" ");
      } else {
        let chars = s.split("");
        shuffle(chars);
        scrambled = chars.join("");
      }
      return scrambled;
    }

    /**
     * Returns valid anagrams of the given string.
     * @param  {String} s    String to get anagrams for.
     * @return {Array}      Array of anagrams, if exists.
     */
    anagram(s) {

    }
  }
  window.LecLit = LecLit;
})();