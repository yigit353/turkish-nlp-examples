#!/bin/bash

#SBATCH -p shortq        # İşin çalıştırılması istenen kuyruk seçilir
#SBATCH -o %j.out      # Çalıştırılan kodun ekran çıktılarını içerir
#SBATCH -e %j.err      # Karşılaşılan hata mesajlarını içerir
#SBATCH -n 20          # Talep edilen işlemci  çekirdek sayısı
#SBATCH --mem-per-cpu=10000

export USER_ROOT=/okyanus/users/ctantug
export DATA_DIR=uncased_shards/tags

export NUM_PROC=20

find "$USER_ROOT/$DATA_DIR" -type f | xargs -I% -P $NUM_PROC -n 1 \
  grep -o 'UNK+Unknown' % | wc -l