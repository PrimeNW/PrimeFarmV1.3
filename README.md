# PrimeFarm

Spigot/Paper 1.8.x icin, oyuncularin arsalarinda kirilan kaktus, seker kamisi
gibi otomatik farmlarin dusen itemlerini yakalayip sanal bir depoya aktaran,
istendiginde satis yapilabilen bir eklenti.

## Nasil calisir?

1. Oyuncu `/pf stora` ile deposuna erişebilir.
2. Depoyu kullanmak için oyun içi parayla ödeme yapar.
3. `/pf settings` ile depolayacağı farm itemini kapatıp, açabilir.
4. O arsa icinde kirilan/dusen KAKTUS ve SEKER KAMISI itemleri (piston, elle
   kirma, fizik nedeniyle dusme - hangi yontemle olursa olsun) dunyaya hic
   dusmeden dogrudan arsa sahibinin deposuna eklenir. Item lag'i olmaz,
   hopper/chest zinciri kurmaya gerek kalmaz.
5. `/pf lang` ile pluginin dilini ayarlayabilir, default dil seçeneği EN olarak ayarlıdır.
6. Vault + bir ekonomi eklentisi (EssentialsX Economy vb.) yuklu ise satislar
   oraya, degilse dahili basit bir bakiye sistemine (`balances.yml`) islenir.

## Komutlar

| Komut | Aciklama |
|---|---|
| `/pf storage` | Arsa secim cubugu verir |
| `/pf lang` | Secili alani arsa olarak kaydeder |
| `/pf settings` | Arsayi siler |
| `/pf reload` | Kendi arsalarini listeler |

## Izinler

- `primefarm.use` (varsayilan: herkes) - komutlari kullanma
- `primefarm.admin` (varsayilan: op) - arsa limitinden muafiyet
`primefarm.page3` 3. sayfaya erişmesini sağlar.
`primefarm.page4` 4. sayfaya erişmesini sağlar.
`primefarm.page5` 5. sayfaya erişmesini sağlar.

## Ayarlar (config.yml)

- `max-zones-per-player`: Oyuncu basina maksimum arsa sayisi
- `prices`: Materyal basina satis fiyati
  `PUMPKIN`, `MELON_BLOCK` gibi baska materyaller de ekleyebilirsin)
