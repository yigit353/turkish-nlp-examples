#!/bin/bash

#SBATCH -p longq        # İşin çalıştırılması istenen kuyruk seçilir
#SBATCH -o %j.out      # Çalıştırılan kodun ekran çıktılarını içerir
#SBATCH -e %j.err      # Karşılaşılan hata mesajlarını içerir
#SBATCH -n 15           # Talep edilen işlemci  çekirdek sayısı
#SBATCH --mem-per-cpu=10000

export USER_ROOT=/okyanus/users/ctantug
export DATA_DIR=uncased_shards

export NUM_PROC=15

find "$USER_ROOT/$DATA_DIR" -type f | xargs -I% -P $NUM_PROC -n 1 \
  mvn exec:java -Dexec.mainClass="zemberek.examples.morphology.DisambiguateSentencesMultiple" \
    -Dexec.args="% true true true"