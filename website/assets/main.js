(() => {
	const KEY = 'flml-theme';
	const btn = document.getElementById('themeToggle');
	const apply = (theme) => {
		if (theme === 'light') {
			document.documentElement.setAttribute('data-theme', 'light');
		} else {
			document.documentElement.removeAttribute('data-theme');
		}
	};
	const current = localStorage.getItem(KEY) || 'dark';
	apply(current);
	if (btn) {
		btn.addEventListener('click', () => {
			const next = (localStorage.getItem(KEY) === 'light') ? 'dark' : 'light';
			localStorage.setItem(KEY, next);
			apply(next);
		});
	}
})();


