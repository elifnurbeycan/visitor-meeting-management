package com.yasarbilgi.visitormeetingmanagment.userpermission.entity;

/**
 * Kullanıcı özelindeki yetki override (istisna) işleminin türünü belirler.
 *
 * GRANT: Rollerden gelmeyen ekstra bir yetkiyi doğrudan kullanıcıya tanımlar.
 * REVOKE: Rollerden gelen bir yetkiyi bu kullanıcı özelinde engeller/geri alır.
 */
public enum OverrideType {

    GRANT,

    REVOKE
}
