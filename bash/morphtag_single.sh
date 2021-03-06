#!/bin/bash

#SBATCH -p longq        # İşin çalıştırılması istenen kuyruk seçilir
#SBATCH -o %j.out      # Çalıştırılan kodun ekran çıktılarını içerir
#SBATCH -e %j.err      # Karşılaşılan hata mesajlarını içerir
#SBATCH -n 1           # Talep edilen işlemci  çekirdek sayısı
#SBATCH --mem-per-cpu=10000

export USER_ROOT=/okyanus/users/ctantug
export DATA_DIR=uncased_shards

mvn exec:java -Dexec.mainClass="zemberek.examples.morphology.DisambiguateSentencesMultiple" \
    -Dexec.args="$USER_ROOT/$DATA_DIR/trl-9021"