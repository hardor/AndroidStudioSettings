package ru.profapp.RanobeReader.JsonApi.RanobeHub;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RanobeHubBook {


    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("name_rus")
    @Expose
    private String nameRus;
    @SerializedName("name_eng")
    @Expose
    private String nameEng;
    @SerializedName("name_original")
    @Expose
    private String nameOriginal;
    @SerializedName("name_others")
    @Expose
    private Object nameOthers;
    @SerializedName("id_context")
    @Expose
    private String idContext;
    @SerializedName("id_original")
    @Expose
    private String idOriginal;
    @SerializedName("description")
    @Expose
    private String description;
    @SerializedName("year")
    @Expose
    private Integer year;
    @SerializedName("id_status")
    @Expose
    private Integer idStatus;
    @SerializedName("volumes")
    @Expose
    private Integer volumes;
    @SerializedName("chapters")
    @Expose
    private Integer chapters;
    @SerializedName("rating")
    @Expose
    private Integer rating;
    @SerializedName("count_download")
    @Expose
    private Integer countDownload;
    @SerializedName("views")
    @Expose
    private Integer views;
    @SerializedName("chapters_origin")
    @Expose
    private String chaptersOrigin;
    @SerializedName("is_prerelease")
    @Expose
    private Integer isPrerelease;
    @SerializedName("is_blocked")
    @Expose
    private Integer isBlocked;
    @SerializedName("created_at")
    @Expose
    private String createdAt;
    @SerializedName("updated_at")
    @Expose
    private String updatedAt;
    @SerializedName("changed_at")
    @Expose
    private String changedAt;
    @SerializedName("deleted_at")
    @Expose
    private Object deletedAt;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNameRus() {
        return nameRus;
    }

    public void setNameRus(String nameRus) {
        this.nameRus = nameRus;
    }

    public String getNameEng() {
        return nameEng;
    }

    public void setNameEng(String nameEng) {
        this.nameEng = nameEng;
    }

    public String getNameOriginal() {
        return nameOriginal;
    }

    public void setNameOriginal(String nameOriginal) {
        this.nameOriginal = nameOriginal;
    }

    public Object getNameOthers() {
        return nameOthers;
    }

    public void setNameOthers(Object nameOthers) {
        this.nameOthers = nameOthers;
    }

    public String getIdContext() {
        return idContext;
    }

    public void setIdContext(String idContext) {
        this.idContext = idContext;
    }

    public String getIdOriginal() {
        return idOriginal;
    }

    public void setIdOriginal(String idOriginal) {
        this.idOriginal = idOriginal;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getIdStatus() {
        return idStatus;
    }

    public void setIdStatus(Integer idStatus) {
        this.idStatus = idStatus;
    }

    public Integer getVolumes() {
        return volumes;
    }

    public void setVolumes(Integer volumes) {
        this.volumes = volumes;
    }

    public Integer getChapters() {
        return chapters;
    }

    public void setChapters(Integer chapters) {
        this.chapters = chapters;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public Integer getCountDownload() {
        return countDownload;
    }

    public void setCountDownload(Integer countDownload) {
        this.countDownload = countDownload;
    }

    public Integer getViews() {
        return views;
    }

    public void setViews(Integer views) {
        this.views = views;
    }

    public String getChaptersOrigin() {
        return chaptersOrigin;
    }

    public void setChaptersOrigin(String chaptersOrigin) {
        this.chaptersOrigin = chaptersOrigin;
    }

    public Integer getIsPrerelease() {
        return isPrerelease;
    }

    public void setIsPrerelease(Integer isPrerelease) {
        this.isPrerelease = isPrerelease;
    }

    public Integer getIsBlocked() {
        return isBlocked;
    }

    public void setIsBlocked(Integer isBlocked) {
        this.isBlocked = isBlocked;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(String changedAt) {
        this.changedAt = changedAt;
    }

    public Object getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Object deletedAt) {
        this.deletedAt = deletedAt;
    }

}
