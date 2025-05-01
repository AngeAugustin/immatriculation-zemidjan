// Script principal pour l'application Zémidjan Immatriculation

document.addEventListener('DOMContentLoaded', function() {
    // Activer tous les tooltips
    var tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    var tooltipList = tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });
    
    // Activer tous les popovers
    var popoverTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="popover"]'));
    var popoverList = popoverTriggerList.map(function (popoverTriggerEl) {
        return new bootstrap.Popover(popoverTriggerEl);
    });
    
    // Fermer automatiquement les alertes après 5 secondes
    setTimeout(function() {
        var alerts = document.querySelectorAll('.alert.alert-dismissible');
        alerts.forEach(function(alert) {
            var alertInstance = bootstrap.Alert.getInstance(alert);
            if (alertInstance) {
                alertInstance.close();
            } else {
                // Fallback pour les navigateurs plus anciens
                alert.classList.remove('show');
                setTimeout(function() {
                    alert.remove();
                }, 150);
            }
        });
    }, 5000);
    
    // Filtrage des tableaux
    setupTableFilters();
    
    // Confirmation de suppression
    setupDeleteConfirmations();
    
    // Validation des formulaires côté client
    setupFormValidation();
});

/**
 * Mise en place des filtres pour les tableaux
 */
function setupTableFilters() {
    // Filtre pour la liste des zémidjans
    var communeFilter = document.getElementById('communeFilter');
    var statusFilter = document.getElementById('statusFilter');
    
    if (communeFilter) {
        communeFilter.addEventListener('change', function() {
            filterZemidjans();
        });
    }
    
    if (statusFilter) {
        statusFilter.addEventListener('change', function() {
            filterZemidjans();
        });
    }
    
    // Filtre pour la liste des cotisations
    var moisFilter = document.getElementById('moisFilter');
    var anneeFilter = document.getElementById('anneeFilter');
    
    if (moisFilter) {
        moisFilter.addEventListener('change', function() {
            filterCotisations();
        });
    }
    
    if (anneeFilter) {
        anneeFilter.addEventListener('change', function() {
            filterCotisations();
        });
    }
}

/**
 * Filtrer la liste des zémidjans
 */
function filterZemidjans() {
    var communeFilter = document.getElementById('communeFilter');
    var statusFilter = document.getElementById('statusFilter');
    var table = document.querySelector('table');
    
    if (!table) return;
    
    var rows = table.querySelectorAll('tbody tr');
    
    rows.forEach(function(row) {
        var communeCell = row.cells[4]; // La colonne "Commune"
        var statusCell = row.cells[6]; // La colonne "Cotisation"
        
        var showRow = true;
        
        // Filtre par commune
        if (communeFilter && communeFilter.value) {
            if (communeCell.textContent.trim() !== communeFilter.value) {
                showRow = false;
            }
        }
        
        // Filtre par statut de cotisation
        if (statusFilter && statusFilter.value) {
            var isAJour = statusCell.querySelector('.badge.bg-success') !== null;
            if ((statusFilter.value === 'true' && !isAJour) || (statusFilter.value === 'false' && isAJour)) {
                showRow = false;
            }
        }
        
        // Afficher ou masquer la ligne
        row.style.display = showRow ? '' : 'none';
    });
}

/**
 * Filtrer la liste des cotisations
 */
function filterCotisations() {
    var moisFilter = document.getElementById('moisFilter');
    var anneeFilter = document.getElementById('anneeFilter');
    var table = document.querySelector('table');
    
    if (!table) return;
    
    var rows = table.querySelectorAll('tbody tr');
    
    rows.forEach(function(row) {
        var periodeCell = row.cells[3]; // La colonne "Période"
        
        var showRow = true;
        
        // Filtre par mois
        if (moisFilter && moisFilter.value) {
            if (!periodeCell.textContent.trim().startsWith(moisFilter.value)) {
                showRow = false;
            }
        }
        
        // Filtre par année
        if (anneeFilter && anneeFilter.value) {
            if (!periodeCell.textContent.trim().endsWith(anneeFilter.value)) {
                showRow = false;
            }
        }
        
        // Afficher ou masquer la ligne
        row.style.display = showRow ? '' : 'none';
    });
}

/**
 * Configuration des confirmations de suppression
 */
function setupDeleteConfirmations() {
    // Pour les boutons qui ne sont pas dans des modals
    var deleteButtons = document.querySelectorAll('button[data-delete-confirm="true"]');
    
    deleteButtons.forEach(function(button) {
        button.addEventListener('click', function(e) {
            if (!confirm('Êtes-vous sûr de vouloir supprimer cet élément ? Cette action est irréversible.')) {
                e.preventDefault();
            }
        });
    });
}

/**
 * Validation des formulaires côté client
 */
function setupFormValidation() {
    // Récupérer tous les formulaires avec la classe needs-validation
    var forms = document.querySelectorAll('.needs-validation');
    
    // Boucle pour empêcher la soumission si le formulaire n'est pas valide
    Array.prototype.slice.call(forms).forEach(function(form) {
        form.addEventListener('submit', function(event) {
            if (!form.checkValidity()) {
                event.preventDefault();
                event.stopPropagation();
            }
            
            form.classList.add('was-validated');
        }, false);
    });
    
    // Validation du mot de passe et de sa confirmation
    var passwordField = document.getElementById('password');
    var confirmPasswordField = document.getElementById('confirmPassword');
    
    if (passwordField && confirmPasswordField) {
        confirmPasswordField.addEventListener('input', function() {
            if (passwordField.value !== confirmPasswordField.value) {
                confirmPasswordField.setCustomValidity('Les mots de passe ne correspondent pas');
            } else {
                confirmPasswordField.setCustomValidity('');
            }
        });
        
        passwordField.addEventListener('input', function() {
            if (confirmPasswordField.value) {
                if (passwordField.value !== confirmPasswordField.value) {
                    confirmPasswordField.setCustomValidity('Les mots de passe ne correspondent pas');
                } else {
                    confirmPasswordField.setCustomValidity('');
                }
            }
        });
    }
}