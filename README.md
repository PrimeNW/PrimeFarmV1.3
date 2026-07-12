# PrimeFarm

Spigot/Paper 1.8.x icin, oyuncularin arsalarinda kirilan kaktus, seker kamisi
gibi otomatik farmlarin dusen itemlerini yakalayip sanal bir depoya aktaran,
istendiginde satis yapilabilen bir eklenti.

## Nasil calisir?

1. Oyuncu `/pf wand` ile bir secim cubugu alir.
2. Sol tik ile 1. nokta, sag tik ile 2. nokta secilir (arsanin iki kosesi).
3. `/pf create <isim>` ile bu alan arsa olarak kaydedilir.
4. O arsa icinde kirilan/dusen KAKTUS ve SEKER KAMISI itemleri (piston, elle
   kirma, fizik nedeniyle dusme - hangi yontemle olursa olsun) dunyaya hic
   dusmeden dogrudan arsa sahibinin deposuna eklenir. Item lag'i olmaz,
   hopper/chest zinciri kurmaya gerek kalmaz.
5. `/pf storage` ile depo GUI'si acilir, urun bazinda ya da "TUMUNU SAT"
   ile toplu satis yapilabilir.
6. Vault + bir ekonomi eklentisi (EssentialsX Economy vb.) yuklu ise satislar
   oraya, degilse dahili basit bir bakiye sistemine (`balances.yml`) islenir.

## Komutlar

| Komut | Aciklama |
|---|---|
| `/pf wand` | Arsa secim cubugu verir |
| `/pf create <isim>` | Secili alani arsa olarak kaydeder |
| `/pf remove <isim>` | Arsayi siler |
| `/pf list` | Kendi arsalarini listeler |
| `/pf storage` | Depo/satis GUI'sini acar |

## Izinler

- `primefarm.use` (varsayilan: herkes) - komutlari kullanma
- `primefarm.admin` (varsayilan: op) - arsa limitinden muafiyet

## Ayarlar (config.yml)

- `max-zones-per-player`: Oyuncu basina maksimum arsa sayisi
- `prices`: Materyal basina satis fiyati
- `tracked-materials`: Otomatik toplanacak materyal listesi (istersen
  `PUMPKIN`, `MELON_BLOCK` gibi baska materyaller de ekleyebilirsin)

## Derleme

Maven kurulu olmasi yeterli:

```bash
mvn clean package
```

Cikan jar dosyasi `target/PrimeFarm.jar` konumunda olusur, sunucunun
`plugins/` klasorune atman yeterli.

**Not:** `pom.xml` icindeki Spigot 1.8.8 API bagimliligi icin BuildTools ile
yerel Maven repository'ne `spigot-1.8.8-R0.1-SNAPSHOT.jar` kurulu olmasi
gerekebilir (Spigot resmi kaynaklarindan `BuildTools.jar` ile
`java -jar BuildTools.jar --rev 1.8.8` calistirarak elde edilir).

## Bilinen sinirlamalar / gelistirme fikirleri

- Arsalar birbiriyle cakisabilir; ilk eslesen arsa esas alinir. Istersen
  `ZoneManager#createZone` icine cakisma kontrolu ekleyebilirsin.
- Su an sadece CACTUS ve SUGAR_CANE takip ediliyor, config'den kolayca
  genisletilebilir (BEETROOT, WHEAT vb. icin BlockBreakEvent bazli ayri
  bir mantik gerekebilir cunku onlar item olarak degil "azalan blok"
  olarak calisir).
- WorldGuard gibi bir bolge eklentisiyle entegrasyon istersen, ZoneManager
  yerine WorldGuard region sorgusu kullanacak sekilde kolayca degistirilebilir.
