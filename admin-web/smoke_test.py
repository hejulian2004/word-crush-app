import os
import tempfile

from playwright.sync_api import sync_playwright


BASE_URL = os.getenv("WORD_CRUSH_ADMIN_URL", "http://127.0.0.1:18081")
ADMIN_USERNAME = os.getenv("WORD_CRUSH_ADMIN_USERNAME", "admin")
ADMIN_PASSWORD = os.getenv("WORD_CRUSH_ADMIN_PASSWORD", "change-this-bootstrap-password")


def main() -> None:
    console_errors: list[str] = []
    with sync_playwright() as playwright:
        browser = playwright.chromium.launch(headless=True)
        page = browser.new_page(viewport={"width": 1440, "height": 960})
        page.on("console", lambda message: console_errors.append(message.text) if message.type == "error" else None)

        page.goto(BASE_URL, wait_until="domcontentloaded")
        page.wait_for_load_state("networkidle")
        page.get_by_label("管理员账号").fill(ADMIN_USERNAME)
        page.get_by_label("登录密码").fill(ADMIN_PASSWORD)
        page.get_by_role("button", name="进入管理端").click()
        page.get_by_text("词库脉搏").wait_for()
        page.screenshot(path=os.path.join(tempfile.gettempdir(), "word-crush-admin-overview.png"), full_page=True)

        page.get_by_role("button", name="用户管理").click()
        page.get_by_text("用户，保持有序。").wait_for()
        page.get_by_role("button", name="单词管理").click()
        page.get_by_text("单词，值得被好好编辑。").wait_for()

        if console_errors:
            raise AssertionError(f"browser console errors: {console_errors}")
        print("browser_smoke=ok overview=ok users=ok words=ok")
        browser.close()


if __name__ == "__main__":
    main()
