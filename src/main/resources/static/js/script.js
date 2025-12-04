/**
 * Основной JavaScript файл для информационно-справочной системы юридической фирмы
 */

/**
 * Показать уведомление
 * @param {string} message - Текст сообщения
 * @param {string} type - Тип уведомления (success, danger, warning, info)
 * @param {number} duration - Длительность отображения в миллисекундах
 */
function showNotification(message, type = 'info', duration = 3000) {
    const alertDiv = document.createElement('div');
    alertDiv.className = `alert alert-${type} alert-dismissible fade show`;
    alertDiv.setAttribute('role', 'alert');
    alertDiv.innerHTML = `
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    `;

    let notificationContainer = document.getElementById('notification-container');
    if (!notificationContainer) {
        notificationContainer = document.createElement('div');
        notificationContainer.id = 'notification-container';
        notificationContainer.style.position = 'fixed';
        notificationContainer.style.top = '20px';
        notificationContainer.style.right = '20px';
        notificationContainer.style.zIndex = '9999';
        notificationContainer.style.maxWidth = '400px';
        document.body.appendChild(notificationContainer);
    }

    notificationContainer.appendChild(alertDiv);

    // Автоматически удалить уведомление через заданное время, можно удалить
    setTimeout(() => {
        alertDiv.classList.remove('show');
        setTimeout(() => {
            alertDiv.remove();
        }, 150);
    }, duration);
}

/**
 * Подтверждение действия
 * @param {string} message - Текст подтверждения
 * @returns {boolean} - Результат подтверждения
 */
function confirmAction(message) {
    return confirm(message);
}

/**
 * Форматирование даты
 * @param {Date|string} date - Дата для форматирования
 * @param {string} format - Формат (short, long, time)
 * @returns {string} - Отформатированная дата
 */
function formatDate(date, format = 'short') {
    const d = typeof date === 'string' ? new Date(date) : date;

    const options = {
        short: { year: 'numeric', month: '2-digit', day: '2-digit' },
        long: { year: 'numeric', month: 'long', day: 'numeric' },
        time: { hour: '2-digit', minute: '2-digit' }
    };

    return d.toLocaleDateString('ru-RU', options[format] || options.short);
}

/**
 * Валидация email
 * @param {string} email - Email для проверки
 * @returns {boolean} - Результат валидации
 */
function validateEmail(email) {
    const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return re.test(String(email).toLowerCase());
}

/**
 * Валидация телефона
 * @param {string} phone - Телефон для проверки
 * @returns {boolean} - Результат валидации
 */
function validatePhone(phone) {
    const re = /^[\+]?[(]?[0-9]{1,4}[)]?[-\s\.]?[(]?[0-9]{1,4}[)]?[-\s\.]?[0-9]{1,9}$/;
    return re.test(String(phone));
}


/**
 * Инициализация валидации форм Bootstrap
 */
function initFormValidation() {
    const forms = document.querySelectorAll('.needs-validation');

    Array.from(forms).forEach(form => {
        form.addEventListener('submit', event => {
            if (!form.checkValidity()) {
                event.preventDefault();
                event.stopPropagation();
            }
            form.classList.add('was-validated');
        }, false);
    });
}

/**
 * Валидация формы в реальном времени
 * @param {HTMLElement} input - Элемент input для валидации
 */
function validateInput(input) {
    if (input.validity.valid) {
        input.classList.remove('is-invalid');
        input.classList.add('is-valid');
    } else {
        input.classList.remove('is-valid');
        input.classList.add('is-invalid');
    }
}

/**
 * Сортировка таблицы
 * @param {HTMLElement} table - Таблица для сортировки
 * @param {number} column - Номер колонки
 * @param {boolean} asc - Направление сортировки (true = по возрастанию)
 */
function sortTable(table, column, asc = true) {
    const tbody = table.querySelector('tbody');
    const rows = Array.from(tbody.querySelectorAll('tr'));

    rows.sort((rowA, rowB) => {
        const cellA = rowA.cells[column].textContent.trim();
        const cellB = rowB.cells[column].textContent.trim();

        const numA = parseFloat(cellA);
        const numB = parseFloat(cellB);

        if (!isNaN(numA) && !isNaN(numB)) {
            return asc ? numA - numB : numB - numA;
        }

        const dateA = Date.parse(cellA);
        const dateB = Date.parse(cellB);

        if (!isNaN(dateA) && !isNaN(dateB)) {
            return asc ? dateA - dateB : dateB - dateA;
        }

        return asc
            ? cellA.localeCompare(cellB, 'ru')
            : cellB.localeCompare(cellA, 'ru');
    });

    tbody.innerHTML = '';
    rows.forEach(row => tbody.appendChild(row));
}

/**
 * Фильтрация таблицы
 * @param {HTMLElement} table - Таблица для фильтрации
 * @param {string} searchText - Текст для поиска
 */
function filterTable(table, searchText) {
    const tbody = table.querySelector('tbody');
    const rows = tbody.querySelectorAll('tr');
    const search = searchText.toLowerCase();

    rows.forEach(row => {
        const text = row.textContent.toLowerCase();
        row.style.display = text.includes(search) ? '' : 'none';
    });

/**
 * Показать модальное окно
 * @param {string} modalId - ID модального окна
 */
function showModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        const bsModal = new bootstrap.Modal(modal);
        bsModal.show();
    }
}

/**
 * Скрыть модальное окно
 * @param {string} modalId - ID модального окна
 */
function hideModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        const bsModal = bootstrap.Modal.getInstance(modal);
        if (bsModal) {
            bsModal.hide();
        }
    }
}


/**
 * Выполнить AJAX запрос
 * @param {string} url - URL для запроса
 * @param {string} method - HTTP метод (GET, POST, PUT, DELETE)
 * @param {Object} data - Данные для отправки
 * @returns {Promise} - Promise с результатом
 */
async function ajaxRequest(url, method = 'GET', data = null) {
    const options = {
        method: method,
        headers: {
            'Content-Type': 'application/json'
        }
    };

    if (data && method !== 'GET') {
        options.body = JSON.stringify(data);
    }

    try {
        const response = await fetch(url, options);

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        return await response.json();
    } catch (error) {
        console.error('AJAX request error:', error);
        showNotification('Произошла ошибка при выполнении запроса', 'danger');
        throw error;
    }
}


/**
 * Подтверждение удаления (универсальная функция)
 * @param {Event} event - Событие клика
 * @param {number} id - ID элемента для удаления
 * @param {string} name - Название элемента
 * @param {string} actionUrl - URL действия
 */
function confirmDelete(event, id, name, actionUrl) {
    event.stopPropagation();

    if (confirm(`Вы действительно хотите удалить "${name}"?\nЭто действие нельзя отменить.`)) {
        const form = document.createElement('form');
        form.method = 'POST';
        form.action = actionUrl.replace('{id}', id);

        const csrfInput = document.createElement('input');
        csrfInput.type = 'hidden';
        csrfInput.name = '_csrf';
        csrfInput.value = getCsrfToken();

        form.appendChild(csrfInput);
        document.body.appendChild(form);
        form.submit();
    }
}

/**
 * Получить CSRF токен
 * @returns {string} - CSRF токен
 */
function getCsrfToken() {
    const token = document.querySelector('meta[name="_csrf"]');
    return token ? token.getAttribute('content') : '';
}

/**
 * Инициализация поиска в реальном времени
 * @param {string} inputId - ID поля ввода
 * @param {string} tableId - ID таблицы
 */
function initLiveSearch(inputId, tableId) {
    const searchInput = document.getElementById(inputId);
    const table = document.getElementById(tableId);
    if (searchInput && table) {
        searchInput.addEventListener('input', (e) => {
            filterTable(table, e.target.value);
        });
    }
}
/**

     Установить минимальную дату для input[type="date"]
     @param {string} inputId - ID элемента input
     @param {Date} minDate - Минимальная дата (по умолчанию - сегодня)
 */
function setMinDate(inputId, minDate = new Date()) {
       const input = document.getElementById(inputId);
       if (input) {
           const year = minDate.getFullYear();
           const month = String(minDate.getMonth() + 1).padStart(2, '0');
           const day = String(minDate.getDate()).padStart(2, '0');
           input.min = '${year}-${month}-${day}';
       }
}
/**Установить минимальную дату и время для input[type="datetime-local"]
 @param {string} inputId - ID элемента input
 */
function setMinDateTime(inputId) {
    const input = document.getElementById(inputId);
    if (input) {
        const now = new Date();
        now.setMinutes(now.getMinutes() - now.getTimezoneOffset());
        input.min = now.toISOString().slice(0, 16);
    }
}


/**

     Сохранить данные в localStorage
     @param {string} key - Ключ
     @param {*} value - Значение
 */
function saveToLocalStorage(key, value) {
    try {
        localStorage.setItem(key, JSON.stringify(value));
    } catch (error) {
        console.error('Error saving to localStorage:', error);
    }
}


/**

     Получить данные из localStorage
     @param {string} key - Ключ
     @returns {*} - Сохраненное значение

 */
function getFromLocalStorage(key) {
    try {
        const item = localStorage.getItem(key);
        return item ? JSON.parse(item) : null;
    } catch (error) {
        console.error('Error reading from localStorage:', error);
        return null;
    }
}

/**

     Удалить данные из localStorage
     @param {string} key - Ключ
 */
function removeFromLocalStorage(key) {
    try {
        localStorage.removeItem(key);
    } catch (error) {
        console.error('Error removing from localStorage:', error);
    }
}


/**

     Инициализация при загрузке DOM
 */
document.addEventListener('DOMContentLoaded', function() {

    initFormValidation();

    const alerts = document.querySelectorAll('.alert:not(.alert-permanent)');
    alerts.forEach(alert => {
        setTimeout(() => {
            const bsAlert = bootstrap.Alert.getOrCreateInstance(alert);
            bsAlert.close();
            }, 5000);
    });

    const deleteButtons = document.querySelectorAll('[data-confirm-delete]');
    deleteButtons.forEach(button => {
        button.addEventListener('click', function(e) {
            if (!confirm('Вы уверены, что хотите удалить этот элемент?')) {
                e.preventDefault();
                e.stopPropagation();
            }
        });
    });

    const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });

    const popoverTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="popover"]'));
    popoverTriggerList.map(function (popoverTriggerEl) {
        return new bootstrap.Popover(popoverTriggerEl);
    });

    const dateInputs = document.querySelectorAll('input[type="date"].future-only');
    dateInputs.forEach(input => {
        setMinDate(input.id);
    });

    const datetimeInputs = document.querySelectorAll('input[type="datetime-local"].future-only');
    datetimeInputs.forEach(input => {
        setMinDateTime(input.id);
    });
    console.log('Legal Firm System initialized successfully');
});

window.LegalFirmSystem = {
        showNotification,
        confirmAction,
        formatDate,
        validateEmail,
        validatePhone,
        sortTable,
        filterTable,
        showModal,
        hideModal,
        ajaxRequest,
        confirmDelete,
        saveToLocalStorage,
        getFromLocalStorage,
        removeFromLocalStorage,
        setMinDate,
        setMinDateTime
}; }