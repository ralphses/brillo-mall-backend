package com.clickstechnology.Brillo.Mall.application.utils;

import com.clickstechnology.Brillo.Mall.application.dto.whatsapp.ButtonInteractive;
import com.clickstechnology.Brillo.Mall.application.dto.whatsapp.CtaUrlInteractive;
import com.clickstechnology.Brillo.Mall.application.dto.whatsapp.ImageMessageRequest;
import com.clickstechnology.Brillo.Mall.application.dto.whatsapp.InteractiveMessageRequest;
import com.clickstechnology.Brillo.Mall.application.dto.whatsapp.ListInteractive;
import com.clickstechnology.Brillo.Mall.application.dto.whatsapp.TextMessageRequest;
import com.clickstechnology.Brillo.Mall.application.dto.whatsapp.VoiceCallInteractive;
import com.clickstechnology.Brillo.Mall.application.dto.whatsapp.shared.Body;
import com.clickstechnology.Brillo.Mall.application.dto.whatsapp.shared.Button;
import com.clickstechnology.Brillo.Mall.application.dto.whatsapp.shared.ButtonAction;
import com.clickstechnology.Brillo.Mall.application.dto.whatsapp.shared.CtaUrlAction;
import com.clickstechnology.Brillo.Mall.application.dto.whatsapp.shared.Footer;
import com.clickstechnology.Brillo.Mall.application.dto.whatsapp.shared.Header;
import com.clickstechnology.Brillo.Mall.application.dto.whatsapp.shared.ImageMessage;
import com.clickstechnology.Brillo.Mall.application.dto.whatsapp.shared.ListAction;
import com.clickstechnology.Brillo.Mall.application.dto.whatsapp.shared.Media;
import com.clickstechnology.Brillo.Mall.application.dto.whatsapp.shared.Reply;
import com.clickstechnology.Brillo.Mall.application.dto.whatsapp.shared.Row;
import com.clickstechnology.Brillo.Mall.application.dto.whatsapp.shared.Section;
import com.clickstechnology.Brillo.Mall.application.dto.whatsapp.shared.TextMessage;
import com.clickstechnology.Brillo.Mall.application.dto.whatsapp.shared.VoiceCallAction;
import com.clickstechnology.Brillo.Mall.application.enums.WhatsappMessageType;

import java.util.List;

public final class WhatsappMessageGenerator {

    private WhatsappMessageGenerator() {
    }

    public static TextMessageRequest createTextMessage(
            String to,
            String body,
            boolean previewUrl
    ) {
        return TextMessageRequest.builder()
                .to(to)
                .text(
                        TextMessage.builder()
                                .body(body)
                                .previewUrl(previewUrl)
                                .build()
                )
                .build();
    }

    public static ImageMessageRequest createImageMessage(
            String to,
            ImageMessage imageMessage
    ) {
        return ImageMessageRequest.builder()
                .to(to)
                .image(imageMessage)
                .build();
    }

    public static InteractiveMessageRequest createCallToActionMessage(
            String to,
            Body body,
            Footer footer,
            String buttonUrl,
            String buttonLabelText,
            Header documentHeader,
            Header imageHeader,
            Header textHeader,
            Header videoHeader
    ) {
        Header header = resolveHeader(
                documentHeader,
                imageHeader,
                textHeader,
                videoHeader,
                body.getText()
        );

        CtaUrlInteractive interactive = CtaUrlInteractive.builder()
                .header(header)
                .body(body)
                .footer(footer)
                .action(
                        CtaUrlAction.builder()
                                .parameters(
                                        CtaUrlAction.Parameters.builder()
                                                .displayText(buttonLabelText)
                                                .url(buttonUrl)
                                                .build()
                                )
                                .build()
                )
                .build();

        return InteractiveMessageRequest.builder()
                .to(to)
                .interactive(interactive)
                .build();
    }

    public static InteractiveMessageRequest createWhatsappCallMessage(
            String to,
            String bodyText,
            String displayText,
            Integer timeToLeave,
            String payload
    ) {
        VoiceCallInteractive interactive = VoiceCallInteractive.builder()
                .body(
                        Body.builder()
                                .text(bodyText)
                                .build()
                )
                .action(
                        VoiceCallAction.builder()
                                .parameters(
                                        VoiceCallAction.Parameters.builder()
                                                .displayText(displayText)
                                                .ttlMinutes(timeToLeave)
                                                .payload(payload)
                                                .build()
                                )
                                .build()
                )
                .build();

        return InteractiveMessageRequest.builder()
                .to(to)
                .interactive(interactive)
                .build();
    }

    public static InteractiveMessageRequest createListMessage(
            String to,
            Header header,
            Body body,
            Footer footer,
            String actionButtonText,
            List<Section> sections
    ) {
        ListInteractive interactive = ListInteractive.builder()
                .header(header)
                .body(body)
                .footer(footer)
                .action(
                        ListAction.builder()
                                .button(actionButtonText)
                                .sections(sections)
                                .build()
                )
                .build();

        return InteractiveMessageRequest.builder()
                .to(to)
                .interactive(interactive)
                .build();
    }

    public static InteractiveMessageRequest createReplyButtonMessage(
            String to,
            Header header,
            Body body,
            Footer footer,
            ButtonAction buttonAction
    ) {
        ButtonInteractive interactive = ButtonInteractive.builder()
                .header(header)
                .body(body)
                .footer(footer)
                .action(buttonAction)
                .build();

        return InteractiveMessageRequest.builder()
                .to(to)
                .interactive(interactive)
                .build();
    }

    public static Row createRow(
            String title,
            String description,
            String rowId
    ) {
        return Row.builder()
                .id(rowId)
                .title(title)
                .description(description)
                .build();
    }

    public static Section createSection(
            List<Row> rows,
            String title
    ) {
        return Section.builder()
                .title(title)
                .rows(rows)
                .build();
    }

    public static Body createBody(String text) {
        return Body.builder()
                .text(text)
                .build();
    }

    public static Button createButton(
            String replyId,
            String replyTitle
    ) {
        return Button.builder()
                .type(WhatsappMessageType.REPLY)
                .reply(
                        Reply.builder()
                                .id(replyId)
                                .title(replyTitle)
                                .build()
                )
                .build();
    }

    public static ButtonAction createButtonAction(
            List<Button> buttons
    ) {
        return ButtonAction.builder()
                .buttons(buttons)
                .build();
    }

    public static Footer createFooter(String text) {
        return Footer.builder()
                .text(text)
                .build();
    }

    public static Header createTextHeader(String text) {
        return Header.builder()
                .type(WhatsappMessageType.TEXT)
                .text(text)
                .build();
    }

    public static Header createImageHeader(String imageUrl) {
        return Header.builder()
                .type(WhatsappMessageType.IMAGE)
                .image(
                        Media.builder()
                                .link(imageUrl)
                                .build()
                )
                .build();
    }

    public static Header createVideoHeader(String videoUrl) {
        return Header.builder()
                .type(WhatsappMessageType.VIDEO)
                .video(
                        Media.builder()
                                .link(videoUrl)
                                .build()
                )
                .build();
    }

    public static Header createDocumentHeader(String documentUrl) {
        return Header.builder()
                .type(WhatsappMessageType.DOCUMENT)
                .document(
                        Media.builder()
                                .link(documentUrl)
                                .build()
                )
                .build();
    }

    private static Header resolveHeader(
            Header documentHeader,
            Header imageHeader,
            Header textHeader,
            Header videoHeader,
            String text
    ) {
        if (documentHeader != null) return documentHeader;
        if (imageHeader != null) return imageHeader;
        if (textHeader != null) return textHeader;
        if (videoHeader != null) return videoHeader;

        return Header.builder()
                .type(WhatsappMessageType.TEXT)
                .text(text)
                .build();
    }
}
