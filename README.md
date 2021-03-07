turkish-nlp-examples
====================

Turkish NLP examples to demonstrate Zemberek-nlp and other related libraries. 

# How to use multi sentence morphological analysis and disamb

With this script 15 files can be morphologically tagged and converted to sentences at the same time if the system allows it. Check the `bash` folder for more details.

```bash
export NUM_PROC=15

find "$USER_ROOT/$DATA_DIR" -maxdepth 1 -type f | xargs -I% -P $NUM_PROC -n 1 \
  mvn exec:java -Dexec.mainClass="zemberek.examples.morphology.DisambiguateSentencesMultiple" \
    -Dexec.args="% true true true"
```

## Arguments

* First argument is the path to file.

* Second argument is if writing `TAGS` file containing morphologically tagged sentences 

* Third argument is if writing `UNKS.TXT` file containing unknown words.

* Fourth arugment is if writing `STATS.TXT` containing statistics about the file and unknown words.
