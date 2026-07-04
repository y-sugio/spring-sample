package demo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

/**
 * Oracle Wallet の初期化。
 * DB（Oracle）への TCPS 接続に必要な Wallet のパスを JVM に設定する。
 *
 * 未決事項（requirements/overview.md §7）:
 * TODO: Wallet ファイル（cwallet.sso / ewallet.p12）の Key Vault からの取得・展開方法。
 *       想定: Key Vault に Base64 で格納 → 起動時に取得して一時ディレクトリへ展開 →
 *       oracle.net.wallet_location にそのパスを設定（spring-cloud-azure-starter-keyvault-secrets
 *       の有効化が前提。build.gradle 参照）
 * TODO: ローカル開発時の Wallet 取得・設定手順
 * TODO: 接続 URL（jdbc:oracle:thin:@tcps://...）は Key Vault のシークレット ${db-url} で管理
 */
@Configuration
public class OracleWalletInitializer {

    private static final Logger log = LoggerFactory.getLogger(OracleWalletInitializer.class);

    /** Wallet 展開先パス。未設定なら何もしない（DB 未接続の現状はこの状態で動かす） */
    @Value("${app.oracle.wallet-location:}")
    private String walletLocation;

    @PostConstruct
    public void init() {
        if (walletLocation == null || walletLocation.isBlank()) {
            log.info("Oracle Wallet は未設定のためスキップします（app.oracle.wallet-location）");
            return;
        }
        // TODO: Key Vault から Wallet を取得して walletLocation へ展開する処理を追加する
        System.setProperty("oracle.net.wallet_location",
                "(SOURCE=(METHOD=file)(METHOD_DATA=(DIRECTORY=" + walletLocation + ")))");
        log.info("Oracle Wallet を設定しました: {}", walletLocation);
    }
}
