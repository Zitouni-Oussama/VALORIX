//// recyclix/backend/service/collector/CollectorNotificationService.java
//
//package com.recyclix.backend.service.collector;
//
//import com.recyclix.backend.dto.notification.NotificationResponseDTO;
//import com.recyclix.backend.exception.ResourceNotFoundException;
//import com.recyclix.backend.exception.UnauthorizedException;
//import com.recyclix.backend.mapper.NotificationMapper;
//import com.recyclix.backend.model.Account;
//import com.recyclix.backend.model.Notification;
//import com.recyclix.backend.repository.AccountRepository;
//import com.recyclix.backend.repository.NotificationRepository;
//import com.recyclix.backend.util.SecurityUtils;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//@Transactional(readOnly = true)
//public class CollectorNotificationService {
//
//    private final NotificationRepository notificationRepository;
//    private final AccountRepository accountRepository;
//    private final NotificationMapper notificationMapper;
//
//    public List<NotificationResponseDTO> getMyNotifications() {
//        Account account = getAuthenticatedCollectorAccount();
//        List<Notification> notifications = notificationRepository.findForAccount(
//                account.getId(),
//                Notification.RoleTypeN.COLLECTOR
//        );
//        return notifications.stream()
//                .map(notificationMapper::toDto)
//                .toList();
//    }
//
//    private Account getAuthenticatedCollectorAccount() {
//        Long accountId = SecurityUtils.getAccountId()
//                .orElseThrow(() -> new UnauthorizedException("Utilisateur non authentifié."));
//        return accountRepository.findById(accountId)
//                .orElseThrow(() -> new ResourceNotFoundException("Compte introuvable."));
//    }
//
//    // Dans CollectorSupportService.java
//
//    @Transactional
//    public void markNotificationAsRead(Long notificationId) {
//        Account account = getAuthenticatedCollectorAccount();
//        Notification notification = notificationRepository.findById(notificationId)
//                .orElseThrow(() -> new ResourceNotFoundException("Notification introuvable"));
//        if (!notification.getAccount().getId().equals(account.getId())) {
//            throw new UnauthorizedException("Cette notification ne vous appartient pas");
//        }
//        notificationRepository.markAsRead(notificationId);
//    }
//
//    @Transactional
//    public void markAllNotificationsAsRead() {
//        Account account = getAuthenticatedCollectorAccount();
//        notificationRepository.markAllAsRead(account.getId());
//    }
//}


































package com.recyclix.backend.service.collector;

import com.recyclix.backend.dto.notification.NotificationResponseDTO;
import com.recyclix.backend.exception.ResourceNotFoundException;
import com.recyclix.backend.exception.UnauthorizedException;
import com.recyclix.backend.mapper.NotificationMapper;
import com.recyclix.backend.model.Account;
import com.recyclix.backend.model.Notification;
import com.recyclix.backend.repository.AccountRepository;
import com.recyclix.backend.repository.NotificationRepository;
import com.recyclix.backend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CollectorNotificationService {

    private final NotificationRepository notificationRepository;
    private final AccountRepository accountRepository;
    private final NotificationMapper notificationMapper;

    public List<NotificationResponseDTO> getMyNotifications() {
        Account account = getAuthenticatedCollectorAccount();
        List<Notification> notifications = notificationRepository.findForAccount(
                account.getId(),
                Notification.RoleTypeN.COLLECTOR
        );
        return notifications.stream().map(notificationMapper::toDto).toList();
    }

    private Account getAuthenticatedCollectorAccount() {
        Long accountId = SecurityUtils.getAccountId()
                .orElseThrow(() -> new UnauthorizedException("Non authentifié"));
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Compte introuvable"));
    }

    @Transactional
    public void markNotificationAsRead(Long notificationId) {
        Account account = getAuthenticatedCollectorAccount();
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification introuvable"));
        if (!notification.getAccount().getId().equals(account.getId())) {
            throw new UnauthorizedException("Cette notification ne vous appartient pas");
        }
        notificationRepository.markAsRead(notificationId);
    }

    @Transactional
    public void markAllNotificationsAsRead() {
        Account account = getAuthenticatedCollectorAccount();
        notificationRepository.markAllAsRead(account.getId());
    }
}