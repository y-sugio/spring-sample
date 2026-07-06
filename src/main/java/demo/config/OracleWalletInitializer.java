package demo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

/**
 * Oracle Wallet の初期化（TCPS 接続になった場合のみ使用する骨組み）。
 *
 * 【前提の変更】DB 接続は ojdbc（thin ドライバ）で行うため、Oracle Client・Wallet とも
 * 不要の見込み。旧環境は Oracle Client 12c（thick/OCI 接続）で Wallet が必要だったが、
 * thin ドライバは 19c 相当の通信規格をカバーする（requirements/overview.md §5）。
 *
 * 未決事項（requirements/overview.md §7）:
 * TODO: DB 側の通信暗号化要件の確認。
 *       - NNE（Native Network Encryption）の場合: このクラスは不要。接続プロパティ
 *         （oracle.net.encryption_client / oracle.net.crypto_checksum_client 等）のみで対応
 *       - TCPS（SSL/TLS）の場合: このクラスを有効化し、Key Vault からの Wallet 取得・展開を実装する
 * TODO: 不要と確定したらこのクラスは削除する
 */
@Configuration
public class OracleWalletInitializer {

    private static final Logger log = LoggerFactory.getLogger(OracleWalletInitializer.class);

    /** Wallet 展開先パス。未設定なら何もしない（thin 接続で Wallet 不要の見込みのため、通常は未設定のまま） */
    @Value("${app.oracle.wallet-location:}")
    private String walletLocation;

    @PostConstruct
    public void init() {
        if (walletLocation == null || walletLocation.isBlank()) {
            log.info("Oracle Wallet は未設定のためスキップします（thin 接続では不要の見込み）");
            return;
        }
        // TODO: TCPS と確定した場合、Key Vault から Wallet を取得して walletLocation へ展開する処理を追加する
        System.setProperty("oracle.net.wallet_location",
                "(SOURCE=(METHOD=file)(METHOD_DATA=(DIRECTORY=" + walletLocation + ")))");
        log.info("Oracle Wallet を設定しました: {}", walletLocation);
    }
}
